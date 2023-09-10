package org.peterpan.rpc.core.transfer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.core.codec.RpcDecoder;
import org.peterpan.rpc.core.codec.RpcEncoder;
import org.peterpan.rpc.core.protocol.RpcProtocol;
import org.peterpan.rpc.core.protocol.body.RpcRequestBody;

import io.netty.channel.socket.SocketChannel;
import org.peterpan.rpc.core.transfer.handler.RpcResponseHandler;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description 客户端的传输层
 */
@Slf4j
public class RpcClientTransfer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcClientTransfer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    /**
     * 发送请求
     * @param protocol 消息
     * @param serviceMetadata 服务
     * @return 当前服务
     * @throws Exception
     */
    public void sendRequest(RpcProtocol<RpcRequestBody> protocol, ServiceMeta serviceMetadata) throws Exception {
        if (serviceMetadata != null) {
            // 连接
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.info("连接 rpc server {} 端口 {} 成功.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                } else {
                    log.error("连接 rpc server {} 端口 {} 失败.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            // 写入数据
            future.channel().writeAndFlush(protocol);
        }
    }


}

//public class RpcClientTransfer {
//    private final Bootstrap bootstrap;
//    private final EventLoopGroup eventLoopGroup;
//    private final ConcurrentHashMap<String, Channel> channelPool = new ConcurrentHashMap<>();
//
//    public RpcClientTransfer() {
//        bootstrap = new Bootstrap();
//        eventLoopGroup = new NioEventLoopGroup(4);
//        bootstrap.group(eventLoopGroup)
//            .channel(NioSocketChannel.class)
//            .handler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                protected void initChannel(SocketChannel socketChannel) throws Exception {
//                    socketChannel.pipeline()
//                        .addLast(new RpcEncoder())
//                        .addLast(new RpcDecoder())
//                        .addLast(new RpcResponseHandler());
//                }
//            });
//    }
//
//    /**
//     * 发送请求
//     *
//     * @param protocol          消息
//     * @param serviceMetadata   服务
//     * @throws Exception
//     */
//    public void sendRequest(RpcProtocol<RpcRequestBody> protocol, ServiceMeta serviceMetadata) throws Exception {
//        if (serviceMetadata != null) {
//            String key = serviceMetadata.getServiceAddr() + ":" + serviceMetadata.getServicePort();
//            Channel channel = channelPool.get(key);
//
//            // 如果连接不存在或者已经关闭，则重新创建连接
//            if (channel == null || !channel.isActive()) {
//                // 连接
//                ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
//                if (future.isSuccess()) {
//                    log.info("连接 rpc server {} 端口 {} 成功.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
//                    channel = future.channel();
//                    channelPool.put(key, channel);
//                } else {
//                    log.error("连接 rpc server {} 端口 {} 失败.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
//                    future.cause().printStackTrace();
//                    eventLoopGroup.shutdownGracefully();
//                    return;
//                }
//            }
//
//            // 写入数据
//            channel.writeAndFlush(protocol);
//        }
//    }
//}