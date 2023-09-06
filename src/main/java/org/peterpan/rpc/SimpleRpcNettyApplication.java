package org.peterpan.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimpleRpcNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleRpcNettyApplication.class, args);
    }

}
