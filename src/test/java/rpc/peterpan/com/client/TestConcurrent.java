package rpc.peterpan.com.client;

import lombok.extern.slf4j.Slf4j;
import rpc.peterpan.com.IDL.Hello.HelloRequest;
import rpc.peterpan.com.IDL.Hello.HelloResponse;
import rpc.peterpan.com.IDL.Hello.HelloService;
import rpc.peterpan.com.util.RpcServiceUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 并发客户端测试
 *
 * 根据个人cpu能力测试，拒绝策略，以及并发性能
 */
@Slf4j
public class TestConcurrent {
   private static final int TOTAL_REQUESTS = 10;
   private static int successCount = 0;

   public static void main(String[] args) throws InterruptedException {
      ExecutorService executorService = Executors.newFixedThreadPool(10);

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
                       10000);
               // 构造出请求对象HelloRequest
               HelloRequest helloRequest = new HelloRequest("peter");
               // rpc调用并返回结果对象HelloResponse
               HelloResponse helloResponse = helloService.hello(helloRequest);
               // 从HelloResponse中获取msg
               String helloMsg = helloResponse.getMsg();
               // 打印msg
               System.out.println(helloMsg);
               if (helloMsg != null ) successCount++;
               log.info("resp={}", helloMsg);
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               double totalRequests = TOTAL_REQUESTS;
               double successRate = (successCount / totalRequests) * 100;
               log.info("TOTAL_REQUESTS={}, SUCCESS={}, FAIL={}, Success Rate={}%", TOTAL_REQUESTS, successCount, TOTAL_REQUESTS - successCount, successRate);
            }
         });
      }

      // 关闭线程池
      executorService.shutdown();
   }
}
