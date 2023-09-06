package org.peterpan.rpc.protocol.handler;

import org.peterpan.rpc.common.RpcFuture;
import org.peterpan.rpc.common.RpcRequestHolder;
import org.peterpan.rpc.common.RpcResponse;
import org.peterpan.rpc.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 响应
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

   @Override
   protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) {
      long requestId = msg.getHeader().getRequestId();
      RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
      future.getPromise().setSuccess(msg.getBody());
   }
}
