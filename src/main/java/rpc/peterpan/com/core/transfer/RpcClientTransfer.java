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
        try {
            Socket socket = new Socket(curServiceMeta.getServiceAddr(), curServiceMeta.getServicePort());

            // 发送【transfer层】
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//            objectOutputStream.writeObject(rpcRequest); // 在发送端发送两个连续的消息，模拟粘包
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            RpcProtocol rpcResponse = (RpcProtocol) objectInputStream.readObject();

            // 校验body长度，预防粘包或者半包问题
            if (rpcResponse.getHeader().getMsgLen() != rpcResponse.getBody().length) {
                throw new IOException("出现粘包或半包异常");
            }

            return rpcResponse;
        } catch (IOException e) {
            // Handle the exception, you can rethrow it or log it, depending on your use case
            throw e;
        }
    }
}

