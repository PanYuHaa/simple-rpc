package org.peterpan.rpc.core.codec.serialization.impl;

import org.peterpan.rpc.core.codec.serialization.IRpcSerialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author PeterPan
 * @date 2023/8/10
 * @description java默认自带的序列化方式
 */

public class JavaSerialization implements IRpcSerialization {

   @Override
   public <T> byte[] serialize(T object) throws IOException {
      if (object == null) {
         throw new NullPointerException(); // 如果对象为空，则抛出空指针异常
      }

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream oos = new ObjectOutputStream(bos)) {
         oos.writeObject(object);
         return bos.toByteArray();
      } catch (IOException e) {
         throw new RuntimeException("Serialization error", e); // 抛出序列化异常，并包装原始异常信息
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException {
      if (bytes == null) {
         throw new NullPointerException(); // 如果字节数组为空，则抛出空指针异常
      }

      try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
           ObjectInputStream ois = new ObjectInputStream(bis)) {
         return (T) ois.readObject();
      } catch (IOException | ClassNotFoundException e) {
         throw new RuntimeException("Deserialization error", e); // 抛出反序列化异常，并包装原始异常信息
      }
   }
}

