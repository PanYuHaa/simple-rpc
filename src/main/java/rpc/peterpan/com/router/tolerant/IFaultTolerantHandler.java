package rpc.peterpan.com.router.tolerant;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 容错策略
 */
public interface IFaultTolerantHandler {
    FaultTolerantContext tolerant(FaultTolerantContext ctx);
}

