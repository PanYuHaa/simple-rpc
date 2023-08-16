package rpc.peterpan.com.registry.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import rpc.peterpan.com.common.ServiceMeta;
import rpc.peterpan.com.config.RpcConfig;
import rpc.peterpan.com.infrastructure.loadbalancer.IServiceLoadBalancer;
import rpc.peterpan.com.infrastructure.loadbalancer.LoadBalancerFactory;
import rpc.peterpan.com.infrastructure.loadbalancer.LoadBalancerType;
import rpc.peterpan.com.registry.IRegistryService;
import rpc.peterpan.com.util.redisKey.RpcServiceNameBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description zk注册中心
 */
public class ZookeeperRegistry implements IRegistryService {

   // 连接失败等待重试时间
   public static final int BASE_SLEEP_TIME_MS = 1000;
   // 重试次数
   public static final int MAX_RETRIES = 3;
   // 跟路径
   public static final String ZK_BASE_PATH = "/peter_rpc";

   // ServiceDiscovery<ServiceMeta> 是一个用于服务发现和管理的高级工具。它可以与注册中心（例如 ZooKeeper）进行交互，实现服务的注册和发现功能
   private final ServiceDiscovery<ServiceMeta> serviceDiscovery;

   /**
    * ZookeeperRegistry 类是基于 ZooKeeper 的注册中心实现，用于服务注册和服务发现。
    * 在构造方法中，它会创建一个与 ZooKeeper 服务器的连接，并启动 CuratorFramework 客户端。
    * CuratorFramework 是 Apache Curator 框架的核心类，它提供了与 ZooKeeper 进行交互的 API。
    * 在连接到 ZooKeeper 服务器后，ZookeeperRegistry 将创建一个 ServiceDiscovery 实例，
    * 用于服务的注册和发现。
    *
    * @throws Exception 可能会抛出异常，例如无法连接到 ZooKeeper 服务器时抛出连接异常。
    */
   public ZookeeperRegistry() throws Exception {
      // 获取注册中心地址
      String registerAddr = RpcConfig.getInstance().getRegisterAddr();

      // 创建与 ZooKeeper 服务器的连接
      CuratorFramework client = CuratorFrameworkFactory.newClient(registerAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));

      // 启动 CuratorFramework 客户端
      client.start();

      // 创建用于序列化服务元数据的 JsonInstanceSerializer 实例
      JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);

      // 创建 ServiceDiscovery 实例，用于服务的注册和发现
      this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
              .client(client) // 设置 CuratorFramework 客户端
              .serializer(serializer) // 设置服务元数据的序列化器
              .basePath(ZK_BASE_PATH) // 设置服务在 ZooKeeper 中的基本路径
              .build();

      // 启动 ServiceDiscovery
      this.serviceDiscovery.start();
   }


   /**
    * 服务注册
    * 将包含服务信息的服务元数据注册到服务发现。
    *
    * @param serviceMeta 包含服务信息的服务元数据。
    * @throws Exception 如果在注册过程中出现错误。
    */
   @Override
   public void register(ServiceMeta serviceMeta) throws Exception {
      ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
              .<ServiceMeta>builder()
              .name(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
              .address(serviceMeta.getServiceAddr())
              .port(serviceMeta.getServicePort())
              .payload(serviceMeta)
              .build();
      serviceDiscovery.registerService(serviceInstance);
   }

   /**
    * 服务注销
    * 从服务发现中注销具有给定服务元数据的服务。
    *
    * @param serviceMeta 要注销的服务元数据。
    * @throws Exception 如果在注销过程中出现错误。
    */
   @Override
   public void unRegister(ServiceMeta serviceMeta) throws Exception {
      ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
              .<ServiceMeta>builder()
              .name(serviceMeta.getServiceName())
              .address(serviceMeta.getServiceAddr())
              .port(serviceMeta.getServicePort())
              .payload(serviceMeta)
              .build();
      serviceDiscovery.unregisterService(serviceInstance);
   }

   /**
    * 服务发现
    * 使用给定的服务名称、调用者的哈希码和负载均衡器类型来发现服务。
    *
    * @param serviceName      要发现的服务名称。
    * @param invokerHashCode  用于负载均衡的调用者哈希码。
    * @param loadBalancerType 要使用的负载均衡器类型。
    * @return 表示发现的服务的ServiceMeta。
    * @throws Exception 如果在服务发现过程中出现错误。
    */
   @Override
   public ServiceMeta discovery(String serviceName, int invokerHashCode, LoadBalancerType loadBalancerType) throws Exception {
      List<ServiceMeta> serviceMetas = listServices(serviceName);
      IServiceLoadBalancer<ServiceMeta> loadBalancer = LoadBalancerFactory.getInstance(loadBalancerType);
      ServiceMeta serviceMeta = loadBalancer.select(serviceMetas, invokerHashCode);
      return serviceMeta;
   }

   /**
    * 获取服务列表
    * 从服务发现中获取具有给定服务名称的服务列表。
    *
    * @param serviceName 要查询实例的服务名称。
    * @return 表示发现的服务列表的ServiceMeta列表。
    * @throws Exception 如果在服务发现过程中出现错误。
    */
   private List<ServiceMeta> listServices(String serviceName) throws Exception {
      Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
      List<ServiceMeta> serviceMetas = serviceInstances.stream().map(serviceMetaServiceInstance -> serviceMetaServiceInstance.getPayload()).collect(Collectors.toList());
      return serviceMetas;
   }

   /**
    * 批量服务发现
    * 发现具有给定服务名称的所有服务。
    *
    * @param serviceName 要发现的服务名称。
    * @return 表示发现的服务列表的ServiceMeta列表。
    */
   @Override
   public List<ServiceMeta> discoveries(String serviceName) {
      try {
         return listServices(serviceName);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return Collections.EMPTY_LIST;
   }

   /**
    * 关闭服务发现
    * 关闭服务发现并释放所有关联的资源。
    *
    * @throws IOException 如果在关闭过程中发生I/O错误。
    */
   @Override
   public void destroy() throws IOException {
      serviceDiscovery.close();
   }

}

