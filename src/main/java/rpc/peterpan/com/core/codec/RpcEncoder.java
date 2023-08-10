package rpc.peterpan.com.core.codec;

import rpc.peterpan.com.core.codec.serialization.IRpcSerialization;
import rpc.peterpan.com.core.codec.serialization.SerializationFactory;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/8/9
 * @description
 */
public class RpcEncoder {

    public static <T> byte[] encode(T body, byte serializationType) throws IOException {
        // 获取序列化工具
        IRpcSerialization IRpcSerialization = SerializationFactory.getRpcSerialization(serializationType);
        return IRpcSerialization.serialize(body);
    }
}
