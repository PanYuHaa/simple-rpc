package org.peterpan.rpc.common;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description
 */
public class RpcServiceNameBuilder {

   // key: 服务名 value: 服务提供方
   public static String buildServiceKey(String serviceName, String serviceVersion) {
      return String.join("$", serviceName, serviceVersion);
   }
}
