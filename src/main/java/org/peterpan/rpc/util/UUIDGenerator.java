package org.peterpan.rpc.util;

import java.util.UUID;

/**
 * @author PeterPan
 * @date 2023/8/18
 * @description UUID 生成器
 */
public class UUIDGenerator {
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateUUID());
    }
}

