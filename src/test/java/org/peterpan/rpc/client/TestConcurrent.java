package org.peterpan.rpc.client;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.IDL.Hello.HelloRequest;
import org.peterpan.rpc.IDL.Hello.HelloResponse;
import org.peterpan.rpc.IDL.Hello.HelloService;
import org.peterpan.rpc.util.RpcServiceUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 并发客户端测试
 * <p>
 * 根据个人cpu能力测试，拒绝策略，以及并发性能
 */
@Slf4j
public class TestConcurrent {
    private static final int TOTAL_REQUESTS = 800;
    private static AtomicInteger successCount = new AtomicInteger(0);
    private static AtomicInteger totalRequestsCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        long startTime = System.nanoTime();
        // 模拟发送10个请求
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executorService.execute(() -> {
                try {
                    // 在这里编写发送请求的逻辑
                    // 获取RpcService
                    HelloService helloService = RpcServiceUtil.getService(HelloService.class,
                            "v1",
                            "RoundRobin",
                            "FailFast",
                            1000);
                    HelloResponse QPSRet = helloService.QPSTest();
//                    // 构造出请求对象HelloRequest
//                    HelloRequest helloRequest = new HelloRequest("peter");
//                    // rpc调用并返回结果对象HelloResponse
//                    HelloResponse helloResponse = helloService.hello(helloRequest);
//               // 从HelloResponse中获取msg
//               String helloMsg = helloResponse.getMsg();
//               // 打印msg
//               System.out.println(helloMsg);
//               if (helloMsg != null ) successCount++;
//               log.info("resp={}", helloMsg);
                    totalRequestsCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 增加成功完成的请求数量
                    successCount.incrementAndGet();

                    // 如果已完成请求数量达到总请求数量，计算并打印 QPS
                    if (totalRequestsCount.get() >= TOTAL_REQUESTS) {
                        long endTime = System.nanoTime();
                        long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
                        double qps = (double) TOTAL_REQUESTS / ((double) executionTime / 1000.0); // 计算 QPS
                        log.info("TOTAL_REQUESTS={}, SUCCESS={}, FAIL={}, Success Rate={}%, QPS={}", TOTAL_REQUESTS, successCount.get(), TOTAL_REQUESTS - successCount.get(), ((double) successCount.get() / (double) TOTAL_REQUESTS) * 100.0, qps);
                    }
                }
            });
        }

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
        log.info("executionTime={}ms", executionTime);

        // 关闭线程池
        executorService.shutdown();
    }
}
