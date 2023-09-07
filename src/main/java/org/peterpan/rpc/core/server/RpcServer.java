package org.peterpan.rpc.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.core.codec.RpcDecoder;
import org.peterpan.rpc.core.codec.RpcEncoder;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.core.transfer.RpcRequestHandler;
import org.peterpan.rpc.core.transfer.RpcServerTransfer;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.RegistryFactory;
import org.peterpan.rpc.util.redisKey.RpcServiceNameBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description rpc服务端传输层
 */
@Slf4j
public class RpcServer {
    // 服务器地址
    private static String serverAddress;
    // 线程池
//    private final ExecutorService threadPool;
    // interfaceName -> interfaceImplementation object
    private final HashMap<String, Object> registeredServiceMap;
    // 注册中心
    private IRegistryService registryCenter;
    // rpc配置中心
    private RpcConfig rpcConfig;

    static {
        try {
            serverAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public RpcServer() throws Exception {
        // 加载组件
        RegistryFactory.init();
        SerializationFactory.init();

        this.registeredServiceMap = new HashMap<String, Object>();
        this.rpcConfig = RpcConfig.getInstance();
        this.registryCenter = RegistryFactory.get(RpcConfig.getInstance().getRegisterType());
    }

    // 参数service就是interface的implementation object
    public void register(Object service, String serviceVersion) throws Exception {
        String interfaceName = service.getClass().getInterfaces()[0].getName();
        ServiceMeta serviceMeta = new ServiceMeta(); // 创建服务元数据对象
        serviceMeta.setServiceAddr(serverAddress); // 设置服务地址
        serviceMeta.setServicePort(Integer.parseInt(rpcConfig.getPort())); // 设置服务端口号
        serviceMeta.setServiceVersion(serviceVersion); // 设置服务版本
        serviceMeta.setServiceName(interfaceName); // 设置服务名称
        // 注册服务到注册中心（使用通用接口register，未来redis或者zk来实现具体内容）
        registryCenter.register(serviceMeta);
        // 缓存服务实例
        registeredServiceMap.put(RpcServiceNameBuilder.buildServiceKey(interfaceName, serviceVersion), service);
    }

    /**
     * 在属性设置之后执行（这是bean生命周期中初始化的一部分），启动 RPC 服务器。
     *
     * @throws Exception
     *
     * 守护线程（Daemon Thread）是在后台运行的线程，它的任务是为其他线程提供服务和支持。当所有非守护线程结束时，守护线程会自动退出，不会阻止 JVM 的正常退出。与之相对的是用户线程（User Thread），它们是程序中的主要线程，当所有用户线程结束时，JVM 才会退出
     * 通过调用 t.setDaemon(true) 方法将线程设置为守护线程。在这段代码中，通过创建一个新的线程 t 并将其设置为守护线程，可以在后台运行 RPC 服务器，即使主线程结束，RPC 服务器仍然可以继续运行，直到所有非守护线程结束或 JVM 被终止
     */
    public void serve() {
        RpcServerTransfer rpcServerTransfer = new RpcServerTransfer(serverAddress, rpcConfig, registeredServiceMap);
        // 在新线程中启动 RPC 服务器
        Thread t = new Thread(() -> {
            try {
                rpcServerTransfer.startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        });
        t.setDaemon(true); // 设置为守护线程
        t.start();
    }
}