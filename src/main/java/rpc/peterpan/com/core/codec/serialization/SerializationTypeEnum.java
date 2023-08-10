package rpc.peterpan.com.core.codec.serialization;

import lombok.Getter;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
public enum SerializationTypeEnum {
   HESSIAN(0x10), // 枚举类型 HESSIAN，对应的 type 值为 0x10

   JSON(0x20), // 枚举类型 JSON，对应的 type 值为 0x20

   JAVA(0x30); // 枚举类型 JAVADEFAULT，对应的 type 值为 0x30

   @Getter
   private final int type; // 枚举类型的 type 属性，用于存储对应的值

   SerializationTypeEnum(int type) {
      this.type = type; // 构造函数，初始化枚举类型的 type 属性
   }

   public static SerializationTypeEnum findByType(byte serializationType) {
      for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) { // SerializationTypeEnum.values() 获取枚举值
         if (typeEnum.getType() == serializationType) { // 根据给定的 serializationType，查找对应的枚举类型
            return typeEnum; // 返回找到的枚举类型
         }
      }
      return HESSIAN; // 默认返回 HESSIAN 枚举类型
   }
}

