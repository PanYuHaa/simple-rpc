package rpc.peterpan.com.core.client;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rpc.peterpan.com.core.codec.RpcDecoder;
import rpc.peterpan.com.core.codec.RpcEncoder;
import rpc.peterpan.com.core.codec.serialization.SerializationTypeEnum;
import rpc.peterpan.com.core.common.MsgType;
import rpc.peterpan.com.core.common.ProtocolConstants;
import rpc.peterpan.com.core.config.RpcConfig;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;
import rpc.peterpan.com.core.protocol.header.MsgHeader;
import rpc.peterpan.com.core.transfer.RpcClientTransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static rpc.peterpan.com.core.common.ProtocolConstants.VERSION;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description 客户端的代理类
 */
public class RpcClientProxy implements InvocationHandler {

   private RpcConfig rpcConfig; // 这里保存 RpcConfig 实例

   public RpcClientProxy(RpcConfig rpcConfig) {
      this.rpcConfig = rpcConfig;
   }

//   private String serviceVersion;
//   private long timeout;
//   private LoadBalancerType loadBalancerType;
//   private IRegistryService IRegistryService;
//   private FaultTolerantType faultTolerantType;
//   private long retryCount;

   @SuppressWarnings("unchecked")
   public <T> T getService(Class<T> clazz) {
      return (T) Proxy.newProxyInstance(
              clazz.getClassLoader(),
              new Class<?>[]{clazz},
              this
      );
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

      // 1、将调用所需信息编码成bytes[]，即有了调用编码【codec层】
      long startTime = System.nanoTime();

//      byte serializationType = (byte) SerializationTypeEnum.JSON.getType();
      byte serializationType = rpcConfig.getSerializationByte();
      byte msgType = (byte) MsgType.REQUEST.ordinal();

      // 构建消息头
      MsgHeader reqHeader = new MsgHeader();
      reqHeader.setMagic(ProtocolConstants.MAGIC);
      reqHeader.setVersion(VERSION);
      reqHeader.setSerialization(serializationType); // 配置文件读取方式，暂时使用JSON
      reqHeader.setMsgType(msgType); // 注意这里是请求REQUEST
      reqHeader.setStatus((byte) 0x1);

      // 构建消息体
      RpcRequestBody rpcRequestBody = RpcRequestBody.builder()
              .interfaceName(method.getDeclaringClass().getName())
              .methodName(method.getName())
              .paramTypes(method.getParameterTypes())
              .parameters(args)
              .build();

      // 序列化
      byte[] bytes = RpcEncoder.encode(rpcRequestBody, serializationType);
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      ObjectOutputStream oos = new ObjectOutputStream(baos);
//      oos.writeObject(rpcRequestBody);
//      byte[] bytes = baos.toByteArray();

      long endTime = System.nanoTime();
      long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
      int byteSize = bytes.length;
      System.out.println("【序列化执行时间】" + executionTime + "ms" + "    " + "【数据大小】" + byteSize + "byte");

      // 2、创建RPC协议，将Header、Body的内容设置好（Body中存放调用编码）【protocol层】
      RpcProtocol rpcRequest = new RpcProtocol();
      rpcRequest.setHeader(reqHeader);
      rpcRequest.setBody(bytes);
//      RpcProtocol<RpcRequestBody> rpcRequest = RpcProtocol.builder()
//              .header(header)
//              .body(bytes)
//              .build();

      // 3、发送RpcRequest，获得RpcResponse【transfer层】
      RpcClientTransfer rpcClient = new RpcClientTransfer();
      RpcProtocol rpcResponse = rpcClient.sendRequest(rpcRequest);

      // 4、解析RpcResponse，也就是在解析rpc协议【protocol层】
      MsgHeader respHeader = rpcResponse.getHeader(); // 来自于响应的header
      byte[] body = rpcResponse.getBody();
      if (respHeader.getVersion() == VERSION) {
         // 将RpcResponse的body中的返回编码，解码成我们需要的对象Object并返回【codec层】
         RpcResponseBody rpcResponseBody = (RpcResponseBody)RpcDecoder.decode(body, respHeader.getSerialization(), respHeader.getMsgType());
         Object retObject = rpcResponseBody.getRetObject();
         return retObject;
//         ByteArrayInputStream bais = new ByteArrayInputStream(body);
//         ObjectInputStream ois = new ObjectInputStream(bais);
//         RpcResponseBody rpcResponseBody = (RpcResponseBody) ois.readObject();
//         Object retObject = rpcResponseBody.getRetObject();
//         return retObject;
      }
      return null;
   }
}

