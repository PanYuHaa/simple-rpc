package org.peterpan.rpc.core.codec.serialization;

import org.peterpan.rpc.spi.ExtensionLoader;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 序列化工厂
 *
 * 简单工厂设计模式，先设好序号，然后根据进来的序号由工厂生产（实例）出对应类
 */
public class SerializationFactory {
    public static IRpcSerialization get(String serialization) throws Exception {

        return ExtensionLoader.getInstance().get(serialization);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(IRpcSerialization.class);
    }
}
