package org.peterpan.rpc.core.codec;

import org.peterpan.rpc.core.codec.serialization.IRpcSerialization;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.common.MsgType;
import org.peterpan.rpc.core.codec.serialization.SerializationTypeEnum;
import org.peterpan.rpc.core.protocol.body.RpcRequestBody;
import org.peterpan.rpc.core.protocol.body.RpcResponseBody;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/8/9
 * @description
 */
public class RpcDecoder {

   public static Object decode(byte[] body, byte serializationType, byte msgType) throws Exception {
      // 处理消息的类型
      MsgType msgTypeEnum = MsgType.findByType(msgType);
      if (msgTypeEnum == null) {
         return null;
      }

      // 获取序列化器
      IRpcSerialization IRpcSerialization = SerializationFactory.get(SerializationTypeEnum.findByType(serializationType).name());
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
