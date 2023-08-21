package org.peterpan.rpc.router.tolerant.impl;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.router.tolerant.FaultTolerantContext;
import org.peterpan.rpc.router.tolerant.IFaultTolerantHandler;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 快速失败容错策略
 */
@Slf4j
public class FailFastFaultTolerantHandler implements IFaultTolerantHandler {
    @Override
    public FaultTolerantContext tolerant(FaultTolerantContext ctx) {
        log.warn("requestId={}, errorMsg={}, 触发 FailFast 策略, serviceKey={}, interface={}", ctx.getRequestId(), ctx.getErrorMsg(), ctx.getServiceKey(), ctx.getMethodName());
        return FaultTolerantContext.builder()
                .count(ctx.getRetryCount())
                .build();
    }
}
