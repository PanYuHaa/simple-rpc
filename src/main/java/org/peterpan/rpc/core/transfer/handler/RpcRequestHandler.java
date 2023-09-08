package org.peterpan.rpc.core.transfer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.MsgStatus;
import org.peterpan.rpc.common.MsgType;
import org.peterpan.rpc.core.protocol.RpcProtocol;
import org.peterpan.rpc.core.protocol.body.RpcRequestBody;
import org.peterpan.rpc.core.protocol.body.RpcResponseBody;
import org.peterpan.rpc.core.protocol.header.MsgHeader;
import org.peterpan.rpc.util.redisKey.RpcServiceNameBuilder;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description 处理消费方发送数据并且调用方法
 *
 * SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> 是一个用于处理 RpcProtocol<RpcRequest> 类型的消息对象的泛型处理器。通过继承它并实现相应的方法，可以定制化处理接收到的消息对象并执行业务逻辑
 *
 * 该处理器是处理 RPC 请求的主要逻辑所在。
 * 它继承自 SimpleChannelInboundHandler<RpcProtocol<RpcRequest>>，接收并处理来自客户端的 RPC 请求。
 * 在 channelRead0 方法中，首先通过异步处理的方式，创建响应协议对象和响应对象，并获取请求协议的消息头。
 * 然后调用 handle 方法处理具体的 RPC 请求，根据请求的处理结果设置响应数据和状态。
 * 最后，将响应协议对象发送给客户端
 *
 * InboundHandler用来处理接收消息，OutboundHandler用来处理发送消息
 *
 * 入站数据 ---> ByteToMessageDecoder ---> SimpleChannelInboundHandler
 * 出站数据 ---> SimpleChannelInboundHandler ---> MessageToByteEncoder
 */
@Slf4j
/**
 * 处理 RPC 请求的处理器。
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequestBody>> {
   private final Map<String, Object> rpcServiceMap; // RPC 服务对象的映射表

   /**
    * 构造函数
    * @param rpcServiceMap RPC 服务对象的映射表
    */
   public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
      this.rpcServiceMap = rpcServiceMap;
   }

   /**
    * 处理接收到的 RPC 请求消息
    */
   @Override
   protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequestBody> protocol) throws Exception {
      // 异步处理
      RpcRequestProcessor.submitRequest(() -> {
         // 创建响应协议对象
         RpcProtocol<RpcResponseBody> resProtocol = new RpcProtocol<>();
         // 创建响应对象
         RpcResponseBody response = new RpcResponseBody();
         // 获取请求协议的消息头
         MsgHeader header = protocol.getHeader();
         // 设置消息类型为响应类型（ordinal方法用来获取枚举在其内部的位置）
         header.setMsgType((byte) MsgType.RESPONSE.ordinal());
         try {
            // 处理请求并获取结果
            Object result = handle(protocol.getBody());
            // 设置响应数据
            response.setData(result);
            // 设置响应状态为成功
            header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            // 设置响应协议的消息头
            resProtocol.setHeader(header);
            // 设置响应协议的消息体
            resProtocol.setBody(response);
         } catch (Throwable throwable) {
            // 处理请求出错，设置响应状态为失败，并设置错误消息
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setMessage(throwable.toString());
            log.error("process request {} error", header.getRequestId(), throwable);
         }
         // 发送响应协议给客户端
         ctx.writeAndFlush(resProtocol); // 用于将数据写入到 Channel，并刷新到底层的网络连接中，用于实现高性能的异步数据传输
      });
   }

   /**
    * 处理具体的 RPC 请求
    */
   private Object handle(RpcRequestBody request) throws Throwable {
      String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getInterfaceName(), request.getServiceVersion());
      // 获取服务信息
      Object serviceBean = rpcServiceMap.get(serviceKey);

      if (serviceBean == null) {
         throw new RuntimeException(String.format("service not exist: %s:%s", request.getInterfaceName(), request.getMethodName()));
      }

      // 获取服务提供方信息并且创建
      Class<?> serviceClass = serviceBean.getClass();
      String methodName = request.getMethodName();
      Class<?>[] parameterTypes = request.getParamsTypes();
      Object[] parameters = request.getParams();

      FastClass fastClass = FastClass.create(serviceClass);
      int methodIndex = fastClass.getIndex(methodName, parameterTypes);
      // 调用方法并返回结果
      return fastClass.invoke(methodIndex, serviceBean, parameters);
   }
}