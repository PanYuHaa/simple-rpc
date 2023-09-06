package org.peterpan.rpc.protocol.serialization;

import org.peterpan.rpc.protocol.serialization.impl.HessianSerialization;
import org.peterpan.rpc.protocol.serialization.impl.JsonSerialization;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 序列化工厂
 *
 * 简单工厂设计模式，先设好序号，然后根据进来的序号由工厂生产（实例）出对应类
 */
public class SerializationFactory {
   public static IRpcSerialization getRpcSerialization(byte serializationType) {
      SerializationTypeEnum typeEnum = SerializationTypeEnum.findByType(serializationType);

      switch (typeEnum) {
         case HESSIAN:
            return new HessianSerialization();
         case JSON:
            return new JsonSerialization();
         default:
            throw new IllegalArgumentException("serialization type is illegal, " + serializationType);
      }
   }
}
