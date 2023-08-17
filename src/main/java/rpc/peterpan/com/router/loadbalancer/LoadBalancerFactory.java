package rpc.peterpan.com.router.loadbalancer;

import rpc.peterpan.com.router.loadbalancer.impl.ConsistentHashLoadBalancer;
import rpc.peterpan.com.router.loadbalancer.impl.RoundRobinLoadBalancer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 负载均衡工厂
 */
public class LoadBalancerFactory {

   private static Map<LoadBalancerType, IServiceLoadBalancer> serviceLoadBalancerMap = new HashMap<>();

   static {
      serviceLoadBalancerMap.put(LoadBalancerType.CONSISTENT_HASH,new ConsistentHashLoadBalancer());
      serviceLoadBalancerMap.put(LoadBalancerType.RoundRobin,new RoundRobinLoadBalancer());
   }
   public static IServiceLoadBalancer getInstance(LoadBalancerType type) throws Exception {

      return serviceLoadBalancerMap.get(type);
   }
}

