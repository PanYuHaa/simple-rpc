package rpc.peterpan.com.core.common;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 协议的常量
 */
public class ProtocolConstants {

   //HEADER_TOTAL_LEN：这个常量表示协议头的总长度。在该协议中，协议头的长度为 18 字节
   public static final int HEADER_TOTAL_LEN = 18;

   //MAGIC：这个常量表示协议的魔数。魔数通常是一个固定的标识符，用于识别和验证协议的有效性。在这个协议中，魔数的值为 0x10
   public static final short MAGIC = 0x10;

   //VERSION：这个常量表示协议的版本号。协议版本号用于区分不同版本的协议，在升级或兼容性方面起到重要作用。在这个协议中，版本号的值为 0x1
   public static final byte VERSION = 0x1;
}
