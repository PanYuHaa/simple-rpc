package org.peterpan.rpc.protocol;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 消息类型
 */
public enum MsgType {
   REQUEST,
   RESPONSE,
   HEARTBEAT;

   public static MsgType findByType(int type) {
      return MsgType.values()[type];
   }
}