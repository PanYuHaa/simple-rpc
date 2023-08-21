package org.peterpan.rpc.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author PeterPan
 * @date 2023/8/18
 * @description
 */

public class RequestIdGenerator {
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    public static long generateRequestId() {
        return counter.incrementAndGet();
    }
}

