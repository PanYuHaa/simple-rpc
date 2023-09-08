package org.peterpan.rpc.core.transfer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.core.codec.RpcDecoder;
import org.peterpan.rpc.core.codec.RpcEncoder;
import org.peterpan.rpc.core.transfer.handler.RpcRequestHandler;

import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description rpc服务端线程执行
 */
@Slf4j
public class RpcServerTransfer {

    private String serverAddress;
    private RpcConfig rpcConfig;
    private HashMap<String, Object> registeredServiceMap;

    public RpcServerTransfer(String serverAddress, RpcConfig rpcConfig, HashMap<String, Object> registeredServiceMap) {
        this.serverAddress = serverAddress;
        this.rpcConfig = rpcConfig;
        this.registeredServiceMap = registeredServiceMap;
    }

    /**
     * 启动 RPC 服务器，监听指定的端口，处理接收到的请求。
     *
     * @throws InterruptedException
     * @throws UnknownHostException boss就是主线程，worker是子线程
     *                              <p>
     *                              在 ChannelPipeline 中，处理器的添加顺序决定了数据在管道中的处理顺序。数据会按照添加顺序依次经过每个处理器进行处理。
     *                              输入数据首先经过 RpcDecoder 进行解码，将字节数据解析为具体的消息对象。然后，消息对象被传递给 RpcRequestHandler 进行处理，进行具体的 RPC 请求处理逻辑。最后，处理结果经过 RpcEncoder 进行编码，将消息对象编码为字节数据，以便发送给客户端
     */
    public void startRpcServer() throws InterruptedException, UnknownHostException {
        int serverPort = Integer.parseInt(rpcConfig.getPort()); // 获取服务器端口号
        EventLoopGroup boss = new NioEventLoopGroup(2); // 创建 Boss 线程组
        EventLoopGroup worker = new NioEventLoopGroup(4); // 创建 Worker 线程组
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
                                    .addLast(new RpcRequestHandler(registeredServiceMap)); // 处理 RPC 请求的处理器
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true); //  TCP 连接的 KeepAlive 选项为 true

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, serverPort).sync(); // 绑定服务器地址和端口，并启动服务器
//            ChannelFuture channelFuture = bootstrap.bind("0.0.0.0", serverPort).sync(); // 绑定服务器地址和端口，并启动服务器
            log.info("server addr {} started on port {}", this.serverAddress, serverPort);
            channelFuture.channel().closeFuture().sync(); // 等待服务器关闭
        } finally {
            boss.shutdownGracefully(); // 关闭 Boss 线程组
            worker.shutdownGracefully(); // 关闭 Worker 线程组
        }
    }
}