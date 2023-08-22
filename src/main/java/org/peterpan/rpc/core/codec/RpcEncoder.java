package org.peterpan.rpc.core.codec;

import org.peterpan.rpc.core.codec.serialization.IRpcSerialization;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.core.codec.serialization.SerializationTypeEnum;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/8/9
 * @description
 */
public class RpcEncoder {

    public static <T> byte[] encode(T body, byte serializationType) throws Exception {
        // 获取序列化工具
        IRpcSerialization IRpcSerialization = SerializationFactory.get(SerializationTypeEnum.findByType(serializationType).name());
        return IRpcSerialization.serialize(body);
    }
}
