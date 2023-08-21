package org.peterpan.rpc.core.codec;

import org.peterpan.rpc.core.codec.serialization.IRpcSerialization;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;

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
