package org.peterpan.rpc.core.transfer;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.core.server.RpcServerWorker;
import org.peterpan.rpc.core.transfer.RejectedExecutionHandler.ExceptionStatusRejectedExecutionHandler;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.RegistryFactory;
import org.peterpan.rpc.registry.RegistryType;
import org.peterpan.rpc.util.redisKey.RpcServiceNameBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description rpc服务端传输层
 */
@Slf4j
public class RpcServerTransfer {
    // 服务器地址
    private static String serverAddress;
    // 线程池
    private final ExecutorService threadPool;
    // interfaceName -> interfaceImplementation object
    private final HashMap<String, Object> registeredService;
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

    public RpcServerTransfer() throws Exception {
        // 加载组件
        RegistryFactory.init();
        SerializationFactory.init();

        int corePoolSize = 10; // 5
        int maximumPoolSize = 50; // 50
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100); // 100
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        // 创建自定义的拒绝策略实例
        RejectedExecutionHandler rejectedExecutionHandler = new ExceptionStatusRejectedExecutionHandler();
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workingQueue,
                threadFactory,
                rejectedExecutionHandler // 设置自定义拒绝策略
        );
        this.registeredService = new HashMap<String, Object>();
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
        registeredService.put(RpcServiceNameBuilder.buildServiceKey(interfaceName, serviceVersion), service);
    }

    public void serve() {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(rpcConfig.getPort()))) {
            log.info("Server Starting... port={}", rpcConfig.getPort());
            Socket handleSocket;
            while ((handleSocket = serverSocket.accept()) != null) {
                log.info("Client Connected, ip={}", handleSocket.getInetAddress());
                threadPool.execute(new RpcServerWorker(handleSocket, registeredService));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}