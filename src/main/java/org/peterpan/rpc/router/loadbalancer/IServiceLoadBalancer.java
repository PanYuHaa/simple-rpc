package org.peterpan.rpc.router.loadbalancer;

import org.peterpan.rpc.common.ServiceMeta;

import java.util.List;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description
 */
public interface IServiceLoadBalancer<T> {

   ServiceMeta select(List<T> servers, int hashCode);
}
