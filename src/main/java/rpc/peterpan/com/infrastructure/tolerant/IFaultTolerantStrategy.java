package rpc.peterpan.com.infrastructure.tolerant;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 容错策略
 */
public interface IFaultTolerantStrategy {

   void handler();
}
