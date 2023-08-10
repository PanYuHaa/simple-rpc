package rpc.peterpan.com.core.protocol.body;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC请求的body内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 调用编码
public class RpcRequestBody implements Serializable {
   private String interfaceName;
   private String methodName;
   private Object[] parameters;
   private Class<?>[] paramTypes;
}
