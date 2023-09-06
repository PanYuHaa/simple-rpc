package org.peterpan.rpc.consumer;

import org.peterpan.rpc.common.*;
import org.peterpan.rpc.protocol.MsgHeader;
import org.peterpan.rpc.protocol.MsgType;
import org.peterpan.rpc.protocol.ProtocolConstants;
import org.peterpan.rpc.protocol.RpcProtocol;
import org.peterpan.rpc.protocol.serialization.SerializationTypeEnum;
import org.peterpan.rpc.registry.RegistryFactory;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.RegistryType;
import org.peterpan.rpc.registry.loadbalancer.LoadBalancerType;
import org.peterpan.rpc.tolerant.FaultTolerantType;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

   private String serviceVersion;
   private long timeout;
   private LoadBalancerType loadBalancerType;
   private IRegistryService IRegistryService;
   private FaultTolerantType faultTolerantType;
   private long retryCount;

   public RpcInvokerProxy(String serviceVersion, long timeout,String faultTolerantType,String loadBalancerType, String registryType,long retryCount) throws Exception {
      this.serviceVersion = serviceVersion;
      this.timeout = timeout;
      this.loadBalancerType = LoadBalancerType.toLoadBalancer(loadBalancerType);
      this.faultTolerantType = FaultTolerantType.toFaultTolerant(faultTolerantType);
      this.IRegistryService = RegistryFactory.getInstance(RegistryType.valueOf(registryType));
      this.retryCount = retryCount;

   }
   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
      // 构建消息头
      MsgHeader header = new MsgHeader();
      long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
      header.setMagic(ProtocolConstants.MAGIC);
      header.setVersion(ProtocolConstants.VERSION);
      header.setRequestId(requestId);
      header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
      header.setMsgType((byte) MsgType.REQUEST.ordinal());
      header.setStatus((byte) 0x1);
      protocol.setHeader(header);

      // 构建请求体
      RpcRequest request = new RpcRequest();
      request.setServiceVersion(this.serviceVersion);
      request.setClassName(method.getDeclaringClass().getName());
      request.setMethodName(method.getName());
      request.setParameterTypes(method.getParameterTypes());
      request.setParams(ObjectUtils.isEmpty(args) ? new Object[0] : args);
      protocol.setBody(request);

      RpcConsumer rpcConsumer = new RpcConsumer();
      // 处理返回数据
      RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);

      RpcRequestHolder.REQUEST_MAP.put(requestId, future);

      String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
      Object[] params = request.getParams();
      // 计算哈希
      int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
      // 根据服务key以及哈希获取服务提供方节点
      ServiceMeta curServiceMeta = IRegistryService.
              discovery(serviceKey, invokerHashCode,loadBalancerType);
      // 供故障转移使用
      List<ServiceMeta> serviceMetas = this.IRegistryService.discoveries(serviceKey);
      long count = 1;
      long retryCount = this.retryCount;
      while (count <= retryCount ){
         try {
            rpcConsumer.sendRequest(protocol, curServiceMeta);
            RpcResponse rpcResponse = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);
            log.info("rpc 调用成功, serviceKey: {}",serviceKey);
            return rpcResponse.getData();
         }catch (Exception e){
            switch (faultTolerantType){
               // 快速失败
               case FailFast:
                  log.warn("rpc 调用失败,触发 FailFast 策略");
                  break;
               // 故障转移
               case Failover:
                  log.warn("rpc 调用失败,第{}次重试",count);
                  count++;
                  serviceMetas.remove(curServiceMeta); // 直接先删除当前节点
                  if (!ObjectUtils.isEmpty(serviceMetas)){
                     curServiceMeta = serviceMetas.get(0);
                  }else {
                     log.warn("rpc 调用失败,无服务可用 serviceKey: {}",serviceKey);
                     count = retryCount;
                  }
                  break;
               // 忽视这次错误
               case Failsafe:
                  return null;
            }
         }
      }

      throw new RuntimeException("RPC调用失败，超过最大重试次数：" + retryCount);
   }
}
