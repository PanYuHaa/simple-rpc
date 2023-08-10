package rpc.peterpan.com.core.common;

/**
 * @author PeterPan
 * @date 2023/8/9
 * @description 消息类型
 *
 * 以接收端视角，你不知道来的信息是响应还是请求，所以需要有个字段来判断
 */
public enum MsgType {
   REQUEST,
   RESPONSE,
   HEARTBEAT;

   public static MsgType findByType(int type) {
      return MsgType.values()[type];
   }
}
