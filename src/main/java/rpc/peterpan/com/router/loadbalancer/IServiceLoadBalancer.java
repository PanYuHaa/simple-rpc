package rpc.peterpan.com.router.loadbalancer;

import rpc.peterpan.com.common.ServiceMeta;

import java.util.List;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description
 */
public interface IServiceLoadBalancer<T> {

   ServiceMeta select(List<T> servers, int hashCode);
}
