package org.peterpan.rpc.consumer;

import org.peterpan.rpc.annotation.EnableConsumerRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author PeterPan
 * @date 2023/7/20
 * @description 消费方测试
 */
@SpringBootApplication
@EnableConsumerRpc
public class consumerApp {

    public static void main(String[] args) {
        SpringApplication.run(consumerApp.class, args);
    }

}
