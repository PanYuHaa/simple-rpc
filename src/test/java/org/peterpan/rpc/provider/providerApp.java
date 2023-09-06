package org.peterpan.rpc.provider;

import org.peterpan.rpc.annotation.EnableProviderRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author PeterPan
 * @date 2023/7/20
 * @description 提供方测试
 */

@SpringBootApplication
@EnableProviderRpc
public class providerApp {
   public static void main(String[] args) {
      SpringApplication.run(providerApp.class, args);
   }
}
