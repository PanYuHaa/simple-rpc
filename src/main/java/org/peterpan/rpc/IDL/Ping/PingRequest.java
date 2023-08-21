package org.peterpan.rpc.IDL.Ping;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description
 */
@Data
@AllArgsConstructor
public class PingRequest implements Serializable {
   private String name;
}
