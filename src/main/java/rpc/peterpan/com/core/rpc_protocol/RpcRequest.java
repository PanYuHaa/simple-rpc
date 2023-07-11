package rpc.peterpan.com.core.rpc_protocol;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC请求协议的内容
 */
@Data
@Builder
// Serializable：对象变成可传输的字节序列
public class RpcRequest implements Serializable {
    // 协议头部分
    private String header;
    // 协议体部分
    private byte[] body;

}
