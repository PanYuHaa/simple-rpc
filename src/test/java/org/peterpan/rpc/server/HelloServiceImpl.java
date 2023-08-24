package org.peterpan.rpc.server;

import org.peterpan.rpc.IDL.Hello.HelloRequest;
import org.peterpan.rpc.IDL.Hello.HelloService;
import org.peterpan.rpc.IDL.Hello.HelloResponse;

public class HelloServiceImpl implements HelloService {
    @Override
    public HelloResponse hello(HelloRequest request) {
        String name = request.getName();
        String retMsg = "hello: " + name;
        HelloResponse response = new HelloResponse(retMsg);
        return response;
    }

    @Override
    public HelloResponse hi(HelloRequest request) {
        String name = request.getName();
        String retMsg = "hi: " + name;
        HelloResponse response = new HelloResponse(retMsg);
        return response;
    }
}
