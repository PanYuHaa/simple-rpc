package org.peterpan.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 *
 * 魔数在通信协议中起着重要的作用，它可以用于标识、识别和验证数据的格式、完整性和合法性，从而保证数据在通信过程中的可靠性和正确性
 */
@Data
public class MsgHeader implements Serializable {
   private short magic; // 魔数
   private byte version; // 协议版本号
   private byte serialization; // 序列化算法
   private byte msgType; // 数据类型
   private byte status; // 状态
   private long requestId; // 请求 ID
   private int msgLen; // 数据长度
}
