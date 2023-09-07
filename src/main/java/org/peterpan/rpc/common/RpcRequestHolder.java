package org.peterpan.rpc.common;

import org.peterpan.rpc.core.protocol.body.RpcResponseBody;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description 请求连接的future的map，根据request为key来获取对应的future接口
 */
public class RpcRequestHolder {
   // 请求id
   public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

   // 绑定请求
   public static final Map<Long, RpcFuture<RpcResponseBody>> REQUEST_MAP = new ConcurrentHashMap<>();
}
