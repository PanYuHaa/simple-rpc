package rpc.peterpan.com.core.client;

import rpc.peterpan.com.common.ServiceMeta;
import rpc.peterpan.com.core.codec.RpcDecoder;
import rpc.peterpan.com.core.codec.RpcEncoder;
import rpc.peterpan.com.common.MsgType;
import rpc.peterpan.com.common.ProtocolConstants;
import rpc.peterpan.com.config.RpcConfig;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;
import rpc.peterpan.com.core.protocol.header.MsgHeader;
import rpc.peterpan.com.core.transfer.RpcClientTransfer;
import rpc.peterpan.com.middleware.loadbalancer.LoadBalancerType;
import rpc.peterpan.com.middleware.registry.IRegistryService;
import rpc.peterpan.com.middleware.registry.RegistryFactory;
import rpc.peterpan.com.middleware.registry.RegistryType;
import rpc.peterpan.com.util.redisKey.RpcServiceNameBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static rpc.peterpan.com.common.ProtocolConstants.VERSION;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description 客户端的代理类
 */
public class RpcClientProxy implements InvocationHandler {

   private RpcConfig rpcConfig; // Rpc配置中心
   private IRegistryService registryCenter; // 注册中心
   private String serviceVersion;
   private LoadBalancerType loadBalancerType;

   public RpcClientProxy(RpcConfig rpcConfig) throws Exception {
      this.rpcConfig = rpcConfig;
      this.registryCenter = RegistryFactory.getInstance(RegistryType.toRegistry(rpcConfig.getRegisterType()));
   }

   @SuppressWarnings("unchecked")
   public <T> T getService(Class<T> clazz, String serviceVersion, String loadBalancerType) {
      this.serviceVersion = serviceVersion;
      this.loadBalancerType = LoadBalancerType.toLoadBalancer(loadBalancerType);
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
              .serviceVersion(serviceVersion)
              .interfaceName(method.getDeclaringClass().getName())
              .methodName(method.getName())
              .paramTypes(method.getParameterTypes())
              .parameters(args)
              .build();

      // 序列化
      byte[] bytes = RpcEncoder.encode(rpcRequestBody, serializationType);

      long endTime = System.nanoTime();
      long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
      int byteSize = bytes.length;
      System.out.println("【序列化执行时间】" + executionTime + "ms" + "    " + "【数据大小】" + byteSize + "byte");

      // 2、创建RPC协议，将Header、Body的内容设置好（Body中存放调用编码）【protocol层】
      RpcProtocol rpcRequest = new RpcProtocol();
      rpcRequest.setHeader(reqHeader);
      rpcRequest.setBody(bytes);

      // 3、发送RpcRequest，获得RpcResponse【transfer层】
      // 与注册中心交互
      String serviceKey = RpcServiceNameBuilder.buildServiceKey(rpcRequestBody.getInterfaceName(), rpcRequestBody.getServiceVersion());
      Object[] params = rpcRequestBody.getParameters();
      // 计算哈希
      int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
      // 根据服务key以及哈希获取服务提供方节点
      ServiceMeta curServiceMeta = registryCenter.
              discovery(serviceKey, invokerHashCode, loadBalancerType);

      RpcClientTransfer rpcClient = new RpcClientTransfer();
      RpcProtocol rpcResponse = rpcClient.sendRequest(rpcRequest, curServiceMeta);

      // 4、解析RpcResponse，也就是在解析rpc协议【protocol层】
      MsgHeader respHeader = rpcResponse.getHeader(); // 来自于响应的header
      byte[] body = rpcResponse.getBody();
      if (respHeader.getVersion() == VERSION) {
         // 将RpcResponse的body中的返回编码，解码成我们需要的对象Object并返回【codec层】
         RpcResponseBody rpcResponseBody = (RpcResponseBody)RpcDecoder.decode(body, respHeader.getSerialization(), respHeader.getMsgType());
         Object retObject = rpcResponseBody.getRetObject();
         return retObject;
      }
      return null;
   }
}

