package org.peterpan.rpc.core.codec.serialization.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.peterpan.rpc.core.codec.serialization.IRpcSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description JSON序列化
 */
public class JsonSerialization implements IRpcSerialization {

    private static final ObjectMapper MAPPER;

    // 静态代码块，在类加载时执行，用于生成 ObjectMapper 对象
    static {
        MAPPER = generateMapper(JsonInclude.Include.ALWAYS);
    }

    // 生成 ObjectMapper 对象的静态方法
    private static ObjectMapper generateMapper(JsonInclude.Include include) {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializationInclusion(include); // 设置序列化包含规则
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 配置反序列化时忽略未知属性
        customMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true); // 配置反序列化时对枚举数值的处理方式
        customMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 设置日期格式化对象
        return customMapper;
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        // 如果对象是 String 类型，则直接转换为字节数组返回
        if (obj instanceof String) {
            return ((String) obj).getBytes();
        } else {
            // 使用 ObjectMapper 对象将对象序列化为 JSON 字符串，并将其转换为字节数组返回
            return MAPPER.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        if (clz.equals(String.class)) {
            // 如果目标类是 String 类型，则直接将字节数组转换为字符串并返回
            return (T) new String(data, StandardCharsets.UTF_8);
        } else {
            // 使用 ObjectMapper 对象将 JSON 字节数组反序列化为指定的类对象并返回
            return MAPPER.readValue(data, clz);
        }
    }
}