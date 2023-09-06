package org.peterpan.rpc.consumer;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description RPC 服务代理的工厂类（他也是模版设计模式，是个模版）
 *
 * RpcReferenceBean 是一个用于创建 RPC 服务代理的工厂类，它负责创建代理对象，并在 Spring 容器初始化时将其注入到消费方的类中，实现了对远程服务的自动引用和调用
 * 联动ConsumerPostProcessor
 */
public class RpcReferenceBean implements FactoryBean<Object> {

   private Class<?> interfaceClass;

   private String serviceVersion;

   private String registryType;

   private String registryAddr;

   private long timeout;

   private Object object;

   private String loadBalancerType;

   private String faultTolerantType;

   private long retryCount;

   @Override
   public Object getObject() throws Exception {
      return object;
   }

   @Override
   public Class<?> getObjectType() {
      return interfaceClass;
   }

   // 创建代理对象
   public void init() throws Exception {

      Object object = Proxy.newProxyInstance(
              interfaceClass.getClassLoader(),
              new Class<?>[]{interfaceClass},
              new RpcInvokerProxy(serviceVersion,timeout,faultTolerantType,loadBalancerType,registryType,retryCount));
      this.object = object;
   }

   public void setRetryCount(long retryCount) {
      this.retryCount = retryCount;
   }

   public void setFaultTolerantType(String faultTolerantType) {
      this.faultTolerantType = faultTolerantType;
   }

   public void setInterfaceClass(Class<?> interfaceClass) {
      this.interfaceClass = interfaceClass;
   }

   public void setServiceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
   }

   public void setRegistryType(String registryType) {
      this.registryType = registryType;
   }

   public void setRegistryAddr(String registryAddr) {
      this.registryAddr = registryAddr;
   }

   public void setTimeout(long timeout) {
      this.timeout = timeout;
   }

   public void setLoadBalancerType(String loadBalancerType) {
      this.loadBalancerType = loadBalancerType;
   }
}
