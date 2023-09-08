package org.peterpan.rpc.client;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.IDL.Hello.HelloRequest;
import org.peterpan.rpc.IDL.Hello.HelloResponse;
import org.peterpan.rpc.IDL.Hello.HelloService;
import org.peterpan.rpc.util.RpcServiceUtil;

/**
 * @author PeterPan
 * @date 2023/9/8
 * @description
 */
@Slf4j
public class QPSTest {

   public static void main(String[] args) throws Exception {
      HelloService helloService = RpcServiceUtil.getService(HelloService.class,
              "v1",
              "RoundRobin",
              "FailFast",
              1000);
      // 构造出请求对象HelloRequest
      HelloRequest helloRequest = new HelloRequest("peter");

      int totalRequests = 750; // 总请求数
      long startTime = System.currentTimeMillis();

      for (int i = 0; i < totalRequests; i++) {
         // rpc调用并返回结果对象HelloResponse
         HelloResponse helloResponse = helloService.hello(helloRequest);
      }

      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;
      double qps = (double) totalRequests / ((double) executionTime / 1000.0);

      log.info("Total Requests: " + totalRequests);
      log.info("Execution Time (ms): " + executionTime);
      log.info("QPS: " + qps);
   }
}
