package rpc.peterpan.com.router.tolerant.impl;

import lombok.extern.slf4j.Slf4j;
import rpc.peterpan.com.router.tolerant.FaultTolerantContext;
import rpc.peterpan.com.router.tolerant.IFaultTolerantHandler;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 忽略错误容错策略
 */
@Slf4j
public class FailsafeFaultTolerantHandler implements IFaultTolerantHandler {
    @Override
    public FaultTolerantContext tolerant(FaultTolerantContext ctx) {
        return null;
    }
}
