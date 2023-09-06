package org.peterpan.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description
 */
@Data
public class RpcResponse implements Serializable {
   private Object data;
   private String message;
}
