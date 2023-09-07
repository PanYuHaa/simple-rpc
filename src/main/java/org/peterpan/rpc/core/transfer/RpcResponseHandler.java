package org.peterpan.rpc.core.transfer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.peterpan.rpc.common.RpcFuture;
import org.peterpan.rpc.common.RpcRequestHolder;
import org.peterpan.rpc.core.protocol.RpcProtocol;
import org.peterpan.rpc.core.protocol.body.RpcResponseBody;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description rpc响应的处理器
 *
 * 入站数据 ---> ByteToMessageDecoder ---> SimpleChannelInboundHandler
 * 出站数据 ---> SimpleChannelInboundHandler ---> MessageToByteEncoder
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponseBody>> {

   @Override
   protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponseBody> msg) {
      long requestId = msg.getHeader().getRequestId();
      RpcFuture<RpcResponseBody> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
      future.getPromise().setSuccess(msg.getBody());
   }
}
