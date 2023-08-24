package org.peterpan.rpc.router.loadbalancer;

import org.peterpan.rpc.router.loadbalancer.impl.ConsistentHashLoadBalancer;
import org.peterpan.rpc.router.loadbalancer.impl.RoundRobinLoadBalancer;
import org.peterpan.rpc.spi.ExtensionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 负载均衡工厂
 */
public class LoadBalancerFactory {
    public static IServiceLoadBalancer get(String serviceLoadBalancer) throws Exception {

        return ExtensionLoader.getInstance().get(serviceLoadBalancer);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(IServiceLoadBalancer.class);
    }
}

