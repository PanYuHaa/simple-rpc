package org.peterpan.rpc.router.loadbalancer;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
public enum LoadBalancerType {
   CONSISTENT_HASH,
   RoundRobin;

   public static LoadBalancerType toLoadBalancer(String loadBalancer) {
      for (LoadBalancerType value : values()) {
         if (value.toString().equals(loadBalancer)) {
            return value;
         }
      }
      return null;
   }
}
