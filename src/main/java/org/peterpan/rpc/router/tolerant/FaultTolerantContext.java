package org.peterpan.rpc.router.tolerant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.peterpan.rpc.common.ServiceMeta;

import java.util.List;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 容错策略上下文
 */
@Data
@Builder
@AllArgsConstructor
public class FaultTolerantContext {
    String serviceKey;
    String methodName;
    String errorMsg;
    ServiceMeta serviceMeta;
    List<ServiceMeta> serviceMetas;
    int count;
    int retryCount;
    long requestId;
}