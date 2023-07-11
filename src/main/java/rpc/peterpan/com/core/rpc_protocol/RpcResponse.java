package rpc.peterpan.com.core.rpc_protocol;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description RPC响应协议的内容
 */
@Data
@Builder
// 为什么要有泛型？因为返回的对象是各种各样类型的
// Serializable：对象变成可传输的字节序列
public class RpcResponse implements Serializable {
   // 协议头部分
   private String header;

   // 协议体部分
   private byte[] body;
}

