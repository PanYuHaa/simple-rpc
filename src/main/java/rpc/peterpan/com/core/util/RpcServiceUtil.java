package rpc.peterpan.com.core.util;

import rpc.peterpan.com.core.client.RpcClientProxy;
import rpc.peterpan.com.core.config.RpcConfig;

/**
 * @author PeterPan
 * @date 2023/8/10
 * @description RPC工具类来封装获取配置过程，动态获得RpcClientProxy代理类
 */
public class RpcServiceUtil {
   private static final RpcConfig rpcConfig = RpcConfig.getInstance();
   private static final RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcConfig);

   public static <T> T getService(Class<T> clazz) {
      return rpcClientProxy.getService(clazz);
   }
}
