package org.peterpan.rpc.registry.impl;

import com.alibaba.fastjson.JSON;
import org.peterpan.rpc.common.RpcServiceNameBuilder;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.config.RpcProperties;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.loadbalancer.LoadBalancerFactory;
import org.peterpan.rpc.registry.loadbalancer.LoadBalancerType;
import org.peterpan.rpc.registry.loadbalancer.IServiceLoadBalancer;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description redis注册中心
 * <p>
 * 思路：
 * 使用集合保存所有服务节点信息
 * 服务启动：节点使用了redis作为注册中心后，将自身信息注册到redis当中(ttl：10秒)，并开启定时任务，ttl/2。
 * 定时任务用于检测各个节点的信息，如果发现节点的时间 < 当前时间，则将节点踢出，如果没有发现，则续签自身节点
 * 将节点踢出后，从服务注册表中找到对应key删除该节点的下的服务数据信息
 * <p>
 * ttl :10秒
 * 定时任务为ttl/2
 * 节点注册后启动心跳检测，检测服务注册的key集合，如果有服务到期，则删除；自身的服务则续签
 * 服务注册后将服务注册到redis以及保存到自身的服务注册key集合，供心跳检测
 *
 * 如果有节点宕机，则其他服务会检测的，如果服务都宕机，则ttl会进行管理
 *
 * TODO：在多个服务同时向公共的注册中心Redis进行注册和更新心跳信息时，可能会出现并发写入的情况，导致数据不一致或者覆盖问题
 *     1. 使用分布式锁：在每次向注册中心注册或更新心跳信息时，先获取一个分布式锁，确保同一时刻只有一个服务能够进行写入操作，其他服务需要等待锁释放后再进行写入。(Redisson实现分布式锁实现，需要做实验)
 *     2. 使用乐观锁：在每次写入操作时，先获取数据的版本号或者时间戳，在写入时检查数据是否被其他服务更新过，如果没有更新，则执行写入操作，否则进行重试或者放弃写入。
 *     3. 使用分布式事务：在涉及多个数据操作的情况下，可以使用分布式事务来确保数据的一致性和完整性。
 *     4. 使用Redis Cluster：将Redis部署为集群模式，利用其内置的分片和复制机制，确保数据的高可用性和一致性
 */
public class RedisRegistry implements IRegistryService {


    private JedisPool jedisPool;

    private String UUID;

    private static final int ttl = 10 * 1000;

    private Set<String> serviceMap = new HashSet<>();

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    /**
     * 注册当前服务,将当前服务ip，端口，时间注册到redis当中，并且开启定时任务
     * 使用集合存储服务节点信息
     */
    public RedisRegistry() {
        // 获取 RpcProperties 的实例
        RpcProperties properties = RpcProperties.getInstance();

        // 获取注册地址并拆分成 host 和 port
        String[] split = properties.getRegisterAddr().split(":");

        // 创建 JedisPoolConfig 对象，并配置最大连接数和最大空闲连接数
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10); // 设置最大连接数为 10
        poolConfig.setMaxIdle(5); // 设置最大空闲连接数为 5

        // 创建 JedisPool 对象，用于连接 Redis 服务
        jedisPool = new JedisPool(poolConfig, split[0], Integer.valueOf(split[1])); // 使用拆分后的 host 和 port 创建 JedisPool

        // 生成 UUID，并保存到实例变量中
        this.UUID = java.util.UUID.randomUUID().toString();

        // 健康监测(redis注册中心开启之后，心跳检测就开始）
        heartbeat();
    }


    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        RpcProperties properties = RpcProperties.getInstance();
        jedis.auth(properties.getRegisterPsw());
        return jedis;
    }

    /**
     * 采用服务端心跳检测的方式！
     * 心跳检测只负责删除过期的，或者你没过期我给你就继续续签
     * 特别的就是自己心跳服务的RedisRegistry节点，如果是遇到了自己通过UUID我们就要继续续签，保证可用性
     */
    private void heartbeat() {
        int startHeartRate = 5; // 定义初始心跳间隔时间，单位为秒
        int heartRate = 5; // 定义心跳间隔时间，单位为秒

        // 使用 ScheduledExecutorService 创建一个定时任务，每隔 heartRate 秒执行一次
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            // 遍历注册的服务节点
            for (String key : serviceMap) {
                // 获取所有服务节点，并查询服务节点的过期时间是否小于当前时间。如果小于当前时间，则表示服务节点已过期，可以将节点下的服务信息都删除
                List<ServiceMeta> serviceNodes = listServices(key);
                Iterator<ServiceMeta> iterator = serviceNodes.iterator();
                while (iterator.hasNext()) {
                    ServiceMeta node = iterator.next();
                    // 1. 删除过期服务：如果节点的过期时间小于当前时间，说明该节点已过期，将其从服务节点列表中移除
                    if (node.getEndTime() < new Date().getTime()) {
                        iterator.remove();
                    }
                    // 2. 自身续签：如果节点的 UUID 与当前 RedisRegistry 的 UUID 相等，说明是当前节点自身，则更新节点的过期时间
                    if (node.getUUID().equals(this.UUID)) {
                        node.setEndTime(node.getEndTime() + ttl / 2); // 将节点的过期时间往后延长 ttl / 2
                    }
                }
                // 如果服务节点列表不为空，则将更新后的服务节点信息重新加载到服务中心中
                if (!ObjectUtils.isEmpty(serviceNodes)) {
                    loadService(key, serviceNodes);
                }
            }

        }, startHeartRate, heartRate, TimeUnit.SECONDS); // 初始延迟为 startHeartRate，之后每隔 heartRate 秒执行一次
    }


    /**
     * 将服务列表加载到Redis中，并设置过期时间。
     *
     * @param key          Redis中的键，用于存储服务列表
     * @param serviceMetas 服务实例列表，包含多个ServiceMeta对象
     *
     * 先删除再添加的操作是为了保证数据的一致性和原子性
     * 1. 假设原本Redis中已经存在一个键key，并存储了一些旧的服务实例列表。
     * 2. 现在需要将新的服务实例列表存储到Redis中，取代旧的列表，并且设置过期时间为10秒。
     * 3. 如果直接使用RPUSH命令将新的服务实例列表插入到Redis列表中，而不删除旧的列表，可能会导致新旧数据混合在一起，造成数据的不一致性
     */
    private void loadService(String key, List<ServiceMeta> serviceMetas) {
        // TODO：可以将lua脚本变成静态的代码，这样不需要每次调用他都进行解析
        // Lua脚本，用于原子性地删除现有键、将服务实例列表依次插入到Redis列表中，并设置过期时间
        String script = "redis.call('DEL', KEYS[1])\n" +
                "for i = 1, #ARGV do\n" +
                "   redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
                "end \n" +
                "redis.call('EXPIRE', KEYS[1],KEYS[2])";

        // 定义存放键和过期时间的列表
        List<String> keys = new ArrayList<>();
        keys.add(key); // Redis中的键
        keys.add(String.valueOf(10)); // 过期时间为10秒

        // 将ServiceMeta对象列表转换为JSON字符串列表
        List<String> values = serviceMetas.stream().map(o -> JSON.toJSONString(o)).collect(Collectors.toList());

        // 获取Jedis连接
        Jedis jedis = getJedis();

        // 执行Lua脚本，将服务实例列表加载到Redis中
        jedis.eval(script, keys, values);

        // 关闭Jedis连接
        jedis.close();
    }


    /**
     * 获取指定键对应的服务节点列表。
     *
     * @param key Redis中保存服务节点的键
     * @return 服务节点列表，每个节点表示一个服务的元数据(ServiceMeta)
     */
    private List<ServiceMeta> listServices(String key) {
        // 获取 Redis 连接实例
        Jedis jedis = getJedis();

        // 从 Redis 中获取指定键的所有值，即服务节点的 JSON 字符串列表
        List<String> list = jedis.lrange(key, 0, -1);

        // 关闭 Redis 连接
        jedis.close();

        // 将 JSON 字符串列表转换为 ServiceMeta 对象列表
        List<ServiceMeta> serviceMetas = list.stream().map(o -> JSON.parseObject(o, ServiceMeta.class)).collect(Collectors.toList());

        // 返回服务节点列表
        return serviceMetas;
    }


    /**
     * 将服务元数据(ServiceMeta)注册到服务中心。
     *
     * @param serviceMeta 待注册的服务元数据
     * @throws Exception 如果在注册过程中出现异常
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        // 构建用于在 Redis 中保存服务节点的键
        String key = RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());

        // 如果服务节点列表中不包含该键，则将该键添加到服务节点列表中
        if (!serviceMap.contains(key)) {
            serviceMap.add(key);
        }

        // 设置服务元数据的UUID和过期时间
        serviceMeta.setUUID(this.UUID);
        serviceMeta.setEndTime(new Date().getTime() + ttl); // 当前时间 + ttl(好短啊ovo)

        // 获取 Redis 连接实例
        Jedis jedis = getJedis();

        // 定义 Lua 脚本，用于将服务元数据以 JSON 字符串形式添加到 Redis 列表，并设置过期时间
        String script = "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], ARGV[2])";

        // 构建 Lua 脚本所需的参数列表
        List<String> value = new ArrayList<>();
        value.add(JSON.toJSONString(serviceMeta)); // 将服务元数据对象转换成 JSON 字符串
        value.add(String.valueOf(10)); // 设置过期时间为 10秒（这里需要根据具体业务需求进行调整）

        // 执行 Lua 脚本，将服务元数据添加到 Redis 列表，并设置过期时间
        jedis.eval(script, Collections.singletonList(key), value);

        // 关闭 Redis 连接
        jedis.close();
    }


    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode, LoadBalancerType loadBalancerType) throws Exception {
        IServiceLoadBalancer<ServiceMeta> loadBalancer = LoadBalancerFactory.getInstance(loadBalancerType);
        List<ServiceMeta> serviceMetas = listServices(serviceName);
        return loadBalancer.select(serviceMetas, invokerHashCode);
    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        return listServices(serviceName);
    }

    @PreDestroy
    public void destroy() {
        // 销毁Jedis连接池
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

}

