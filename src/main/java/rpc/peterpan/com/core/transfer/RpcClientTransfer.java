package rpc.peterpan.com.core.transfer;

import rpc.peterpan.com.common.ServiceMeta;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;
import rpc.peterpan.com.middleware.registry.IRegistryService;
import rpc.peterpan.com.middleware.registry.RegistryFactory;
import rpc.peterpan.com.middleware.registry.RegistryType;
import rpc.peterpan.com.util.redisKey.RpcServiceNameBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description
 */
// 传入protocol层的RpcRequest，输出protocol层的RpcResponse
public class RpcClientTransfer {
   public RpcProtocol sendRequest(RpcProtocol rpcRequest, ServiceMeta curServiceMeta) throws Exception {

      try (Socket socket = new Socket(curServiceMeta.getServiceAddr(), curServiceMeta.getServicePort())) { // "localhost", 9000
         // 发送【transfer层】
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
         objectOutputStream.writeObject(rpcRequest);
         objectOutputStream.flush();

         RpcProtocol rpcResponse = (RpcProtocol) objectInputStream.readObject();

         return rpcResponse;

      } catch (IOException | ClassNotFoundException e) {
         e.printStackTrace();
         return null;
      }
   }
}

