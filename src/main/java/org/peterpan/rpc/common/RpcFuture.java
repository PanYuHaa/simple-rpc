package org.peterpan.rpc.common;

import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 表示 RPC 调用的未来结果。它包含了结果的 Promise 和超时时间。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcFuture<T> {
    private Promise<T> promise;
    private long timeout;
}
