package rpc.peterpan.com.server;


import rpc.peterpan.com.IDL.Hello.HelloService;
import rpc.peterpan.com.core.server.RpcServer;

public class TestServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer(); // 真正的rpc server
        HelloService helloService = new HelloServiceImpl(); // 包含需要处理的方法的对象
        rpcServer.register(helloService); // 向rpc server注册对象里面的所有方法
//        PingService pingService = new PingServiceImpl();
//        rpcServer.register(pingService);

        rpcServer.serve(9000);
    }
}
