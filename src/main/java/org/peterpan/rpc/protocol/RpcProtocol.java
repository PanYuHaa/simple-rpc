package org.peterpan.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
@Data
public class RpcProtocol<T> implements Serializable {
   private MsgHeader header;
   private T body;
}
