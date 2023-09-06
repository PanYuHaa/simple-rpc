package org.peterpan.rpc;

import org.peterpan.rpc.protocol.MsgStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SimpleRpcNettyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void enumTest() {
        System.out.println((byte) MsgStatus.SUCCESS.ordinal());
    }
}