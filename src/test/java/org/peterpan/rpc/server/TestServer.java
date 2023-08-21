package org.peterpan.rpc.server;


import org.peterpan.rpc.IDL.Hello.HelloService;
import org.peterpan.rpc.core.transfer.RpcServerTransfer;

public class TestServer {
    public static void main(String[] args) throws Exception {
        RpcServerTransfer rpcServerTransfer = new RpcServerTransfer(); // 真正的rpc server
        HelloService helloService = new HelloServiceImpl(); // 包含需要处理的方法的对象
        rpcServerTransfer.register(helloService, "v1"); // 向rpc server注册对象里面的所有方法(手动注册)
//        PingService pingService = new PingServiceImpl();
//        rpcServer.register(pingService);

        rpcServerTransfer.serve();
    }
}
