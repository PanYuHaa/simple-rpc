package org.peterpan.rpc.config;

import lombok.Data;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 配置信息(netty端口 + 注册中心)
 */
@Data
public class RpcProperties {

    /**
     * netty 端口
     */
    private int port;

    /**
     * 注册中心地址
     */
    private String registerAddr;

    /**
     * 注册中心类型
     */
    private String registerType;

    /**
     * 注册中心密码
     */
    private String registerPsw;

    //单例设计模式中的懒汉式，被调用的时候如果没有就创建一个
    static RpcProperties rpcProperties;

    // TODO：代优化成双重校验
    public static RpcProperties getInstance() {
        if (rpcProperties == null) {
            rpcProperties = new RpcProperties();
        }
        return rpcProperties;
    }

    private RpcProperties() {
    }

}

