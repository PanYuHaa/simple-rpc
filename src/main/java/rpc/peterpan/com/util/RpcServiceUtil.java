package rpc.peterpan.com.util;

import rpc.peterpan.com.core.client.RpcClientProxy;
import rpc.peterpan.com.config.RpcConfig;

/**
 * @author PeterPan
 * @date 2023/8/10
 * @description RPC工具类来封装获取配置过程，动态获得RpcClientProxy代理类
 */
public class RpcServiceUtil {
   private static final RpcConfig rpcConfig = RpcConfig.getInstance();
   private static final RpcClientProxy rpcClientProxy;

   static {
      try {
         rpcClientProxy = new RpcClientProxy(rpcConfig);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public static <T> T getService(Class<T> clazz, String serviceVersion, String loadBalancerType) {
      return rpcClientProxy.getService(clazz, serviceVersion, loadBalancerType);
   }
}
