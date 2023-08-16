package rpc.peterpan.com.core.transfer;

import rpc.peterpan.com.common.ServiceMeta;
import rpc.peterpan.com.core.protocol.RpcProtocol;

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
        Socket socket = new Socket(curServiceMeta.getServiceAddr(), curServiceMeta.getServicePort()); // "localhost", 9000
//        if (true) {
//            throw new IOException("Something went wrong"); // 测试future内部错误
//        }
        // 发送【transfer层】
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream.writeObject(rpcRequest);
        objectOutputStream.flush();

        RpcProtocol rpcResponse = (RpcProtocol) objectInputStream.readObject();

        return rpcResponse;
    }
}

