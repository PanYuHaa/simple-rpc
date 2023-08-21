package org.peterpan.rpc.client;

import org.peterpan.rpc.IDL.Hello.HelloRequest;
import org.peterpan.rpc.IDL.Hello.HelloResponse;
import org.peterpan.rpc.IDL.Hello.HelloService;
import org.peterpan.rpc.util.RpcServiceUtil;

public class TestClient {
    public static void main(String[] args) throws Exception {
        // 获取RpcService
        HelloService helloService = RpcServiceUtil.getService(HelloService.class,
                "v1",
                "RoundRobin",
                "FailFast",
                1000);
        // 构造出请求对象HelloRequest
        HelloRequest helloRequest = new HelloRequest("peter");
        // rpc调用并返回结果对象HelloResponse(因为他是代理类，所以调用的同时他会激活invoke中的逻辑)
        HelloResponse helloResponse = helloService.hello(helloRequest);
        // 从HelloResponse中获取msg
        String helloMsg = helloResponse.getMsg();
        // 打印msg
        System.out.println(helloMsg);

        // 调用hi方法
        HelloResponse hiResponse = helloService.hi(helloRequest);
        String hiMsg = hiResponse.getMsg();
        System.out.println(hiMsg);

        // 调用ping方法
//        PingService pingService = proxy.getService(PingService.class);
//        PingRequest pingRequest = new PingRequest("tom");
//        PingResponse pingResponse = pingService.ping(pingRequest);
//        String pingMsg = pingResponse.getMsg();
//        System.out.println(pingMsg);
    }
}
