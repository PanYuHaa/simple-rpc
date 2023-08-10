package rpc.peterpan.com.core.codec;

import rpc.peterpan.com.core.codec.serialization.IRpcSerialization;
import rpc.peterpan.com.core.codec.serialization.SerializationFactory;
import rpc.peterpan.com.core.common.MsgType;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/8/9
 * @description
 */
public class RpcDecoder {

   public static Object decode(byte[] body, byte serializationType, byte msgType) throws IOException {
      // 处理消息的类型
      MsgType msgTypeEnum = MsgType.findByType(msgType);
      if (msgTypeEnum == null) {
         return null;
      }

      // 获取序列化器
      IRpcSerialization IRpcSerialization = SerializationFactory.getRpcSerialization(serializationType);
      // 根据消息类型进行处理(如果消息类型过多可以使用策略+工厂模式进行管理)
      switch (msgTypeEnum) {
         // 请求消息
         case REQUEST:
            RpcRequestBody request = IRpcSerialization.deserialize(body, RpcRequestBody.class);
            if (request != null) {
               return request;
            }
            break;
         // 响应消息
         case RESPONSE:
            Object response = IRpcSerialization.deserialize(body, RpcResponseBody.class);
            if (response != null) {
               return response;
            }
            break;
      }
      return null;
   }
}
