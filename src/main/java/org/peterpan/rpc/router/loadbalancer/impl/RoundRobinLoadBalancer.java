package org.peterpan.rpc.router.loadbalancer.impl;

import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.router.loadbalancer.IServiceLoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 轮询算法
 *
 * 轮询负载均衡器实现类
 * 实现 IServiceLoadBalancer 接口，用于从服务列表中按照轮询的方式选择一个服务实例。
 * 轮询负载均衡算法会依次选择服务列表中的每个服务实例，按顺序分发请求，直到所有服务实例都被选择过一次，
 * 然后再从头开始轮询。适用于负载相对均衡的场景。
 */
public class RoundRobinLoadBalancer implements IServiceLoadBalancer {

   private static AtomicInteger roundRobinId = new AtomicInteger(0);

   /**
    * 从服务列表中选择一个服务实例
    *
    * @param servers   服务列表(根据服务的名称进行查询到的服务的List，所以都是相同的名字不同的部署)
    * @param hashCode  用于负载均衡的调用者哈希码（在轮询算法中不会使用该参数）
    * @return 选中的服务实例的 ServiceMeta 对象
    *
    * 轮询负载均衡器实现了一个简单的负载均衡算法，通过不断递增轮询ID，选择服务列表中的不同服务实例。这些服务实例在功能上是相同的，只是部署在不同的节点上，由serviceAddr和servicePort的组合来标识不同的节点
    */
   @Override
   public ServiceMeta select(List servers, int hashCode) {
      // 1.获取服务的数量
      int size = servers.size();
      // 2.根据当前轮询ID取余服务长度得到具体服务
      roundRobinId.incrementAndGet(); // 每次调用轮询负载均衡器都增加轮询ID，incrementAndGet()方法，它能保证在多线程环境下每次增加操作都是原子的
      if (roundRobinId.get() == Integer.MAX_VALUE){ // 防止轮询ID溢出，达到最大值时重新从0开始
         roundRobinId.set(0);
      }
      // 3.根据轮询ID取余服务数量得到具体的服务索引，从服务列表中选择对应索引的服务实例
      return (ServiceMeta) servers.get(roundRobinId.get() % size);
   }
}

