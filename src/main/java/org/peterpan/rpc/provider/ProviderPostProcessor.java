package org.peterpan.rpc.provider;

import org.peterpan.rpc.annotation.RpcService;
import org.peterpan.rpc.common.RpcServiceNameBuilder;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.config.RpcProperties;
import org.peterpan.rpc.protocol.codec.RpcDecoder;
import org.peterpan.rpc.protocol.codec.RpcEncoder;
import org.peterpan.rpc.protocol.handler.RpcRequestHandler;
import org.peterpan.rpc.registry.RegistryFactory;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.RegistryType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 提供方的处理器
 *
 * setEnvironment()：该方法在 Bean 实例化之后、属性注入之前被调用
 * afterPropertiesSet()：该方法在 setEnvironment() 方法之后、属性注入之前被调用
 * postProcessBeforeInitialization()：该方法在 afterPropertiesSet() 方法之后、初始化之前被调用
 * 执行流程是(setEnvironment() -> postProcessBeforeInitialization() -> afterPropertiesSet() -> startRpcServer())
 */
@Slf4j
/**
 * 服务提供方后置处理器，用于启动 RPC 服务器和注册服务到注册中心。
 */
public class ProviderPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {

    RpcProperties rpcProperties; // RPC 配置属性

    private static String serverAddress; // 服务器地址

    static {
        try {
            serverAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Object> rpcServiceMap = new HashMap<>(); // 存储服务实例的映射

    /**
     * 设置环境，获取 RPC 配置属性。
     * environment 对象的来源取决于你的 Spring 配置方式，可以是属性文件、系统环境变量等
     *
     * @param environment Spring 环境
     */
    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "rpc.";
        Integer port = Integer.valueOf(environment.getProperty(prefix + "port")); // 获取端口号
        String registerAddr = environment.getProperty(prefix + "register-addr"); // 获取注册中心地址
        String registerType = environment.getProperty(prefix + "register-type"); // 获取注册中心类型
        String registerPsw = environment.getProperty(prefix + "register-psw"); // 获取注册中心密码
        RpcProperties properties = RpcProperties.getInstance();
        properties.setPort(port);
        properties.setRegisterAddr(registerAddr);
        properties.setRegisterType(registerType);
        properties.setRegisterPsw(registerPsw);
        rpcProperties = properties;
    }

    /**
     * 在 Bean 初始化之前处理，用于注册服务到注册中心。
     *
     * @param bean     要初始化的 Bean 对象
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 对象
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass(); // 获取 Bean 的 Class 对象
        RpcService rpcService = beanClass.getAnnotation(RpcService.class); // 获取 RpcService 注解
        if (rpcService != null) { // 如果 Bean 上存在 RpcService 注解
            // TODO：这里未来可以结合下面的代码改成指定or全覆盖（默认）
            String interfaceName = beanClass.getInterfaces()[0].getName(); // 获取 Bean 实现的接口名，默认为第一个接口
            if (!rpcService.serviceInterface().equals(void.class)) { // 如果 RpcService 注解中指定了 serviceInterface 属性
                interfaceName = rpcService.serviceInterface().getName(); // 使用指定的 serviceInterface 接口名
            }
            String serviceVersion = rpcService.serviceVersion(); // 获取 RpcService 注解中的 serviceVersion 属性

            try {
                int servicePort = rpcProperties.getPort(); // 获取服务端口号
                IRegistryService IRegistryService = RegistryFactory.getInstance(RegistryType.valueOf(rpcProperties.getRegisterType())); // 获取注册中心服务实例
                ServiceMeta serviceMeta = new ServiceMeta(); // 创建服务元数据对象
                serviceMeta.setServiceAddr(serverAddress); // 设置服务地址
                serviceMeta.setServicePort(servicePort); // 设置服务端口号
                serviceMeta.setServiceVersion(serviceVersion); // 设置服务版本
                serviceMeta.setServiceName(interfaceName); // 设置服务名称
                // 注册服务到注册中心（使用通用接口register，未来redis或者zk来实现它都可以）
                IRegistryService.register(serviceMeta);
                // 缓存服务实例
                rpcServiceMap.put(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
                log.info("register server {} version {}", interfaceName, serviceVersion);
            } catch (Exception e) {
                log.error("failed to register service {}", serviceVersion, e);
            }
        }
        return bean; // 返回处理后的 Bean 对象
    }

    // TODO：上面是单接口实现的，我现在可以一个服务实现多个接口，批量注入，待测试
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        Class<?> beanClass = bean.getClass(); // 获取 Bean 的 Class 对象
//        RpcService rpcService = beanClass.getAnnotation(RpcService.class); // 获取 RpcService 注解
//        if (rpcService != null) { // 如果 Bean 上存在 RpcService 注解
//            Class<?>[] interfaces = beanClass.getInterfaces(); // 获取 Bean 实现的所有接口
//            for (Class<?> interfaceClass : interfaces) {
//                String interfaceName = interfaceClass.getName(); // 获取接口名
//                String serviceVersion = rpcService.serviceVersion(); // 获取 RpcService 注解中的 serviceVersion 属性
//
//                try {
//                    int servicePort = rpcProperties.getPort(); // 获取服务端口号
//                    RegistryService registryService = RegistryFactory.getInstance(RegistryType.valueOf(rpcProperties.getRegisterType())); // 获取注册中心服务实例
//                    ServiceMeta serviceMeta = new ServiceMeta(); // 创建服务元数据对象
//                    serviceMeta.setServiceAddr(serverAddress); // 设置服务地址
//                    serviceMeta.setServicePort(servicePort); // 设置服务端口号
//                    serviceMeta.setServiceVersion(serviceVersion); // 设置服务版本
//                    serviceMeta.setServiceName(interfaceName); // 设置服务名称
//                    // 注册服务到注册中心
//                    registryService.register(serviceMeta);
//                    // 缓存服务实例
//                    rpcServiceMap.put(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
//                    log.info("register server {} version {}", interfaceName, serviceVersion);
//                } catch (Exception e) {
//                    log.error("failed to register service {}", serviceVersion, e);
//                }
//            }
//        }
//        return bean; // 返回处理后的 Bean 对象
//    }

    /**
     * 在属性设置之后执行（这是bean生命周期中初始化的一部分），启动 RPC 服务器。
     *
     * @throws Exception
     *
     * 守护线程（Daemon Thread）是在后台运行的线程，它的任务是为其他线程提供服务和支持。当所有非守护线程结束时，守护线程会自动退出，不会阻止 JVM 的正常退出。与之相对的是用户线程（User Thread），它们是程序中的主要线程，当所有用户线程结束时，JVM 才会退出
     * 通过调用 t.setDaemon(true) 方法将线程设置为守护线程。在这段代码中，通过创建一个新的线程 t 并将其设置为守护线程，可以在后台运行 RPC 服务器，即使主线程结束，RPC 服务器仍然可以继续运行，直到所有非守护线程结束或 JVM 被终止
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 在新线程中启动 RPC 服务器
        Thread t = new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        });
        t.setDaemon(true); // 设置为守护线程
        t.start();
    }

    /**
     * 启动 RPC 服务器，监听指定的端口，处理接收到的请求。
     *
     * @throws InterruptedException
     * @throws UnknownHostException
     *
     * boss就是主线程，worker是子线程
     *
     * 在 ChannelPipeline 中，处理器的添加顺序决定了数据在管道中的处理顺序。数据会按照添加顺序依次经过每个处理器进行处理。
     * 输入数据首先经过 RpcDecoder 进行解码，将字节数据解析为具体的消息对象。然后，消息对象被传递给 RpcRequestHandler 进行处理，进行具体的 RPC 请求处理逻辑。最后，处理结果经过 RpcEncoder 进行编码，将消息对象编码为字节数据，以便发送给客户端
     */
    private void startRpcServer() throws InterruptedException, UnknownHostException {
        int serverPort = rpcProperties.getPort(); // 获取服务器端口号
        EventLoopGroup boss = new NioEventLoopGroup(); // 创建 Boss 线程组
        EventLoopGroup worker = new NioEventLoopGroup(); // 创建 Worker 线程组
        try {
            ServerBootstrap bootstrap = new ServerBootstrap(); // 创建服务器启动引导类 ServerBootstrap，用于配置和启动服务器
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class) // 设置服务器通道的类型为 NIO（java的NIO为IO多路复用）
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        // 设置处理新连接的读写处理器，其中包含了添加 RpcEncoder（RPC 编码器）、RpcDecoder（RPC 解码器）和 RpcRequestHandler（处理 RPC 请求的处理器）到通道的处理流水线中
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder()) // RPC 编码器
                                    .addLast(new RpcDecoder()) // RPC 解码器
                                    .addLast(new RpcRequestHandler(rpcServiceMap)); // 处理 RPC 请求的处理器
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true); //  TCP 连接的 KeepAlive 选项为 true

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, serverPort).sync(); // 绑定服务器地址和端口，并启动服务器
            log.info("server addr {} started on port {}", this.serverAddress, serverPort);
            channelFuture.channel().closeFuture().sync(); // 等待服务器关闭
        } finally {
            boss.shutdownGracefully(); // 关闭 Boss 线程组
            worker.shutdownGracefully(); // 关闭 Worker 线程组
        }
    }
}


