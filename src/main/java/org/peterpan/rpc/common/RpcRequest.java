package org.peterpan.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description
 */
@Data
public class RpcRequest implements Serializable {
   private String serviceVersion;
   private String className;
   private String methodName;
   private Object[] params;
   private Class<?>[] parameterTypes;
}
