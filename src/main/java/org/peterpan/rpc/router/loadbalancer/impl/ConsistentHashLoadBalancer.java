package org.peterpan.rpc.router.loadbalancer.impl;

import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.router.loadbalancer.IServiceLoadBalancer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description zk一致性哈希
 */
public class ConsistentHashLoadBalancer implements IServiceLoadBalancer {

   // 物理节点映射的虚拟节点,为了解决哈希倾斜
   private final static int VIRTUAL_NODE_SIZE = 10;
   private final static String VIRTUAL_NODE_SPLIT = "$";

   @Override
   public ServiceMeta select(List servers, int hashCode) {
      TreeMap<Integer, ServiceMeta> ring = makeConsistentHashRing(servers);
      return allocateNode(ring, hashCode);
   }

   private ServiceMeta allocateNode(TreeMap<Integer, ServiceMeta> ring, int hashCode) {
      // 获取最近的哈希环上节点位置，ceilingEntry 方法用于获取 TreeMap 中大于或等于给定键的最小键值对（Entry）。如果 TreeMap 中没有大于或等于给定键的键值对，那么该方法将返回 null。
      Map.Entry<Integer, ServiceMeta> entry = ring.ceilingEntry(hashCode);
      if (entry == null) {
         // 如果没有找到则使用最小的节点
         entry = ring.firstEntry();
      }
      return entry.getValue();
   }

   /**
    * 将所有服务实例添加到一致性哈希环上，并生成虚拟节点
    * 这里每次调用都需要构建哈希环是为了扩展(服务提供方)
    * @param servers 服务实例列表
    * @return 一致性哈希环
    */
   private TreeMap<Integer, ServiceMeta> makeConsistentHashRing(List<ServiceMeta> servers) {
      TreeMap<Integer, ServiceMeta> ring = new TreeMap<>();
      for (ServiceMeta instance : servers) {
         for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
            ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
         }
      }
      return ring;
   }

   /**
    * 根据服务实例信息构建缓存键
    * @param serviceMeta
    * @return
    */
   private String buildServiceInstanceKey(ServiceMeta serviceMeta) {

      return String.join(":", serviceMeta.getServiceAddr(), String.valueOf(serviceMeta.getServicePort()));
   }
}

