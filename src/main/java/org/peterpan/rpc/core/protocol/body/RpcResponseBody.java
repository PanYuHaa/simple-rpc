package org.peterpan.rpc.core.protocol.body;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC响应的body协议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 返回值编码
public class RpcResponseBody implements Serializable {
   private Object data;
   private String message;
}

