package org.peterpan.rpc.core.protocol;

import lombok.Data;
import org.peterpan.rpc.core.protocol.header.MsgHeader;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 协议内容
 */
@Data
public class RpcProtocol implements Serializable {
   private MsgHeader header;
   private byte[] body;
}
