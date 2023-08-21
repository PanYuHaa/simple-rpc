package org.peterpan.rpc.router.tolerant.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.router.tolerant.FaultTolerantContext;
import org.peterpan.rpc.router.tolerant.IFaultTolerantHandler;

import java.util.List;


/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 故障转移容错策略
 */
@Slf4j
public class FailoverFaultTolerantHandler implements IFaultTolerantHandler {

    @Override
    public FaultTolerantContext tolerant(FaultTolerantContext ctx) {
        int curCount = ctx.getCount();
        ServiceMeta curServiceMeta = ctx.getServiceMeta();
        List<ServiceMeta> serviceMetas = ctx.getServiceMetas();

        log.warn("requestId={}, errorMsg={}, 触发 Failover 策略, 第{}次重试, serviceKey={}, interface={}", ctx.getRequestId(), ctx.getErrorMsg(), curCount, ctx.getServiceKey(), ctx.getMethodName());
        curCount++;
        serviceMetas.remove(curServiceMeta);
        if (!ObjectUtils.isEmpty(serviceMetas)) {
            curServiceMeta = serviceMetas.get(0);
        } else {
            log.warn("requestId={}, errorMsg={}, 触发 Failover 策略, 无服务可用, serviceKey={}, interface={}", ctx.getRequestId(), ctx.getErrorMsg(), ctx.getServiceKey(), ctx.getMethodName());
            curCount = ctx.getRetryCount();
        }
        return FaultTolerantContext.builder()
                .count(curCount)
                .serviceMeta(curServiceMeta)
                .build();
    }
}
