package org.peterpan.rpc.filter.impl.service;


import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.filter.FilterData;
import org.peterpan.rpc.filter.IServiceBeforeFilter;

import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description token拦截器
 */
public class ServiceTokenFilter implements IServiceBeforeFilter {

    @Override
    public void doFilter(FilterData filterData) {
        final Map<String, Object> attachments = filterData.getClientAttachments();
        final Map<String, Object> serviceAttachments = RpcConfig.getInstance().getServiceAttachments();
        if (!attachments.getOrDefault("token","").equals(serviceAttachments.getOrDefault("token",""))){
            throw new IllegalArgumentException("token不正确");
        }
    }

}
