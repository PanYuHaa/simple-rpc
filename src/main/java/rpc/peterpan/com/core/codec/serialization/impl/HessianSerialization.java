package rpc.peterpan.com.core.codec.serialization.impl;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import rpc.peterpan.com.core.codec.serialization.IRpcSerialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description Hessian 序列化
 *
 * Hessian优点：1、二进制格式 2、跨语言支持 3、对象图像传输 4、高效性能
 */
public class HessianSerialization implements IRpcSerialization {

   @Override
   public <T> byte[] serialize(T object) {
      if (object == null) {
         throw new NullPointerException(); // 如果对象为空，则抛出空指针异常
      }
      byte[] results;

      HessianSerializerOutput hessianOutput;
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
         hessianOutput = new HessianSerializerOutput(os);
         hessianOutput.writeObject(object); // 将对象写入 Hessian 输出流
         hessianOutput.flush(); // 刷新输出流
         results = os.toByteArray(); // 将输出流的内容转换为字节数组
      } catch (Exception e) {
         throw new RuntimeException("Serialization error", e); // 抛出序列化异常，并包装原始异常信息
      }

      return results; // 返回序列化后的字节数组
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> T deserialize(byte[] bytes, Class<T> clz) {
      if (bytes == null) {
         throw new NullPointerException(); // 如果字节数组为空，则抛出空指针异常
      }
      T result;

      try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
         HessianSerializerInput hessianInput = new HessianSerializerInput(is);
         result = (T) hessianInput.readObject(clz); // 从 Hessian 输入流中读取对象
      } catch (Exception e) {
         throw new RuntimeException("Deserialization error", e); // 抛出反序列化异常，并包装原始异常信息
      }

      return result; // 返回反序列化后的对象
   }
}
