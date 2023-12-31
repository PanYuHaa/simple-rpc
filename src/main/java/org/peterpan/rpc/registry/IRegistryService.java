package org.peterpan.rpc.registry;

import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.router.loadbalancer.LoadBalancerType;

import java.io.IOException;
import java.util.List;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 注册中心接口
 */
public interface IRegistryService {

    /**
     * 服务注册
     *
     * @param serviceMeta
     * @throws Exception
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务注销
     *
     * @param serviceMeta
     * @throws Exception
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 查询服务,可以封装成对象
     *
     * @param serviceName      key
     * @param invokerHashCode  哈希
     * @param loadBalancerType 负载策略
     * @return
     * @throws Exception
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode, String loadBalancerType) throws Exception;

    /**
     * 获取 serviceName 下的所有服务
     *
     * @param serviceName
     * @return
     */

    List<ServiceMeta> discoveries(String serviceName);

    /**
     * 关闭
     *
     * @throws IOException
     */
    void destroy() throws IOException;
}

