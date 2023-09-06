package org.peterpan.rpc.protocol.codec;

import org.peterpan.rpc.protocol.MsgHeader;
import org.peterpan.rpc.protocol.RpcProtocol;
import org.peterpan.rpc.protocol.serialization.IRpcSerialization;
import org.peterpan.rpc.protocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 编码器
 *
 * 该处理器负责将 RPC 请求对象编码成字节流，以便在网络上传输。
 * 它继承自 MessageToByteEncoder，在 encode 方法中对请求对象进行编码，并将编码后的字节流写入到 ByteBuf 中。
 *
 * 该方法会在数据发送之前被调用，你可以根据自己的需求将消息对象转换为字节数据，并将其写入 ByteBuf 对象中，供后续的数据传输使用
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        // 获取消息头类型
        MsgHeader header = msg.getHeader();
        // 写入魔数(安全校验，可以参考java中的CAFEBABE)
        byteBuf.writeShort(header.getMagic());
        // 写入版本号
        byteBuf.writeByte(header.getVersion());
        // 写入序列化方式(接收方需要依靠具体哪个序列化进行序列化)
        byteBuf.writeByte(header.getSerialization());
        // 写入消息类型(接收放根据不同的消息类型进行不同的处理方式)
        byteBuf.writeByte(header.getMsgType());
        // 写入状态
        byteBuf.writeByte(header.getStatus());
        // 写入请求id(请求id可以用于记录异步回调标识,具体需要回调给哪个请求)
        byteBuf.writeLong(header.getRequestId());
        IRpcSerialization IRpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());
        byte[] data = IRpcSerialization.serialize(msg.getBody());
        // 写入数据长度(接收方根据数据长度读取数据内容)
        byteBuf.writeInt(data.length);
        // 写入数据
        byteBuf.writeBytes(data);
    }
}

