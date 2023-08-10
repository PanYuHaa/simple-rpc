package rpc.peterpan.com.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import rpc.peterpan.com.core.codec.serialization.SerializationTypeEnum;

/**
 * @author PeterPan
 * @date 2023/8/10
 * @description 配置中心，采用懒汉式的单例，减少内存使用
 */

public class RpcConfig {
    private static RpcConfig instance;
    private Properties properties;

    private RpcConfig() {
        loadProperties();
    }

    public static RpcConfig getInstance() {
        if (instance == null) {
            synchronized (RpcConfig.class) {
                if (instance == null) {
                    instance = new RpcConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream inputStream = RpcConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte getSerializationByte() {
        String serializationType = properties.getProperty("serializationType");
        System.out.println("采用的[" + serializationType + "]序列化方式");
        // 根据读取的字符串值，映射为对应的byte值，暂时支持两种，默认HESSIAN
        if ("JSON".equalsIgnoreCase(serializationType)) {
            return (byte) SerializationTypeEnum.JSON.getType();
        } else if ("HESSIAN".equalsIgnoreCase(serializationType)) {
            return (byte) SerializationTypeEnum.HESSIAN.getType();
        }
        return (byte) SerializationTypeEnum.JAVA.getType(); // 默认值
    }

    public static void main(String[] args) {
        RpcConfig rpcConfig = new RpcConfig();
        byte serializationByte = rpcConfig.getSerializationByte();
        System.out.println("Serialization Byte: " + serializationByte);
    }
}