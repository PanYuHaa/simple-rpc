package org.peterpan.rpc.IDL.Hello.impl;

import org.peterpan.rpc.IDL.Hello.HelloRequest;
import org.peterpan.rpc.IDL.Hello.HelloResponse;
import org.peterpan.rpc.IDL.Hello.HelloService;

/**
 * @author PeterPan
 * @date 2023/9/7
 * @description
 */
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

   @Override
   public HelloResponse QPSTest() {
      return new HelloResponse("success request!");
   }


}

