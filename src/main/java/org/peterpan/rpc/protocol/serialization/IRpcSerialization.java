package org.peterpan.rpc.protocol.serialization;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 序列化接口
 *
 * <T> 表示范型方法
 */
public interface IRpcSerialization {
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
