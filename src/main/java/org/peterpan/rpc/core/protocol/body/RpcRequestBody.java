package org.peterpan.rpc.core.protocol.body;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC请求的body协议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 调用编码
public class RpcRequestBody implements Serializable {
   private String serviceVersion;
   private String interfaceName;
   private String methodName;
   private Object[] params;
   private Class<?>[] paramsTypes;
   private Map<String,Object> serviceAttachments;
   private Map<String,Object> clientAttachments;
}
