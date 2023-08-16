package rpc.peterpan.com.infrastructure.tolerant;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
public enum FaultTolerantType {
    Failover,
    FailFast,
    Failsafe;

    public static FaultTolerantType toFaultTolerant(String loadBalancer) {
        for (FaultTolerantType value : values()) {
            if (value.toString().equals(loadBalancer)) {
                return value;
            }
        }
        return null;
    }
}
