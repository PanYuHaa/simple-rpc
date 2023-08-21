package org.peterpan.rpc.util.redisKey;

/**
 * @author PeterPan
 * @date 2023/8/10
 * @description RpcService的redisKey拼接工具
 */
public class RpcServiceNameBuilder {

   // key: 服务名 value: 服务提供方
   public static String buildServiceKey(String serviceName, String serviceVersion) {
      return String.join("_", serviceName, serviceVersion);
   }
}
