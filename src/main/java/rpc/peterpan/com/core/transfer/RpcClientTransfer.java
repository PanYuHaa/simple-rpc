package rpc.peterpan.com.core.transfer;

import rpc.peterpan.com.common.ServiceMeta;
import rpc.peterpan.com.common.StatusConstants;
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
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            Socket socket = new Socket(curServiceMeta.getServiceAddr(), curServiceMeta.getServicePort());

            // 发送【transfer层】
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
//            objectOutputStream.writeObject(rpcRequest); // 在发送端发送两个连续的消息，模拟粘包
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            RpcProtocol rpcResponse = (RpcProtocol) objectInputStream.readObject();

            // 校验header和body，如果为null则表示服务端发来的拒绝策略
            if (rpcResponse.getHeader().getStatus() == StatusConstants.EXCEPTION) {
                throw new IOException("服务线程池执行拒绝策略");
            }

//            // 校验body长度，预防粘包问题
//            if (rpcResponse.getHeader().getMsgLen() < rpcResponse.getBody().length) {
//                throw new IOException("出现粘包异常");
//            }
//
//            // 校验body长度，预防半包问题
//            if (rpcResponse.getHeader().getMsgLen() > rpcResponse.getBody().length) {
//                throw new IOException("出现半包异常");
//            }

            return rpcResponse;
        } catch (IOException e) {
            // Handle the exception, you can rethrow it or log it, depending on your use case
            throw e;
        } finally {
            objectOutputStream.close();
            objectInputStream.close();
        }
    }
}

