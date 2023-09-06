package org.peterpan.rpc.protocol.codec;

import org.peterpan.rpc.common.RpcRequest;
import org.peterpan.rpc.common.RpcResponse;
import org.peterpan.rpc.protocol.MsgHeader;
import org.peterpan.rpc.protocol.MsgType;
import org.peterpan.rpc.protocol.ProtocolConstants;
import org.peterpan.rpc.protocol.RpcProtocol;
import org.peterpan.rpc.protocol.serialization.IRpcSerialization;
import org.peterpan.rpc.protocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 解码器
 *
 * ByteToMessageDecoder 是 Netty 框架中的一个类，用于将字节数据解码为消息对象
 * decode() 方法，可以根据协议定义对字节数据进行解码，并将解码后的消息对象传递给后续的处理器进行进一步处理
 *
 * 该方法会在数据接收时被调用，你可以根据协议的规定从 ByteBuf 中读取字节数据，并将其解析为具体的消息对象
 */
public class RpcDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 如果可读字节数少于协议头长度，说明还没有接收完整个协议头，直接返回
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        // 标记当前读取位置，便于后面回退
        in.markReaderIndex();

        // 读取魔数字段
        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        // 读取版本字段
        byte version = in.readByte();
        // 读取序列化类型
        byte serializeType = in.readByte();
        // 读取消息类型
        byte msgType = in.readByte();
        // 读取响应状态
        byte status = in.readByte();
        // 读取请求 ID
        long requestId = in.readLong();
        // 读取消息体长度
        int dataLength = in.readInt();
        // 如果可读字节数小于消息体长度，说明还没有接收完整个消息体，回退并返回
        if (in.readableBytes() < dataLength) {
            // 回退标记位置
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        // 读取数据
        in.readBytes(data);

        // 处理消息的类型
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        // 构建消息头
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        // 获取序列化器
        IRpcSerialization IRpcSerialization = SerializationFactory.getRpcSerialization(serializeType);
        // 根据消息类型进行处理(如果消息类型过多可以使用策略+工厂模式进行管理)
        switch (msgTypeEnum) {
            // 请求消息
            case REQUEST:
                RpcRequest request = IRpcSerialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            // 响应消息
            case RESPONSE:
                RpcResponse response = IRpcSerialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
        }
    }
}
