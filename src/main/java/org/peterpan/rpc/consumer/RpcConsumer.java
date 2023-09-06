package org.peterpan.rpc.consumer;

import org.peterpan.rpc.common.RpcRequest;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.protocol.RpcProtocol;
import org.peterpan.rpc.protocol.codec.RpcDecoder;
import org.peterpan.rpc.protocol.codec.RpcEncoder;
import org.peterpan.rpc.protocol.handler.RpcResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 消费方发送数据
 */
@Slf4j
public class RpcConsumer {
   private final Bootstrap bootstrap;
   private final EventLoopGroup eventLoopGroup;

   public RpcConsumer() {
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
   public void sendRequest(RpcProtocol<RpcRequest> protocol, ServiceMeta serviceMetadata) throws Exception {
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
