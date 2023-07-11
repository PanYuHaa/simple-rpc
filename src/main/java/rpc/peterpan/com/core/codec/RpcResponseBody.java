package rpc.peterpan.com.core.codec;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC响应的body内容
 */
@Data
@Builder
// 返回值编码
public class RpcResponseBody implements Serializable {
   private Object retObject;
}

