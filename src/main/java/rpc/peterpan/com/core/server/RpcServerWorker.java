package rpc.peterpan.com.core.server;

import rpc.peterpan.com.core.codec.RpcDecoder;
import rpc.peterpan.com.core.codec.RpcEncoder;
import rpc.peterpan.com.core.codec.serialization.SerializationTypeEnum;
import rpc.peterpan.com.core.common.MsgType;
import rpc.peterpan.com.core.common.ProtocolConstants;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;
import rpc.peterpan.com.core.protocol.header.MsgHeader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;

import static rpc.peterpan.com.core.common.ProtocolConstants.VERSION;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description
 */
public class RpcServerWorker implements Runnable {

    private Socket socket;
    private HashMap<String, Object> registeredService;

    public RpcServerWorker(Socket socket, HashMap<String, Object> registeredService) {
        this.socket = socket;
        this.registeredService = registeredService;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            // 1、Transfer层获取到RpcRequest消息【transfer层】
            RpcProtocol rpcRequest = (RpcProtocol) objectInputStream.readObject();
            MsgHeader reqHeader = rpcRequest.getHeader();

            // 2、解析版本号，并判断【protocol层】
            if (reqHeader.getVersion() == VERSION) {

                // 3、将rpcRequest中的body部分解码出来变成RpcRequestBody【codec层】
                long startTime = System.nanoTime();

                byte[] body = rpcRequest.getBody();
                RpcRequestBody rpcRequestBody = (RpcRequestBody)RpcDecoder.decode(body, reqHeader.getSerialization(), reqHeader.getMsgType());

                long endTime = System.nanoTime();
                long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
                int byteSize = body.length;
                System.out.println("【反序列化执行时间】" + executionTime + "ms" + "    " + "【数据大小】" + byteSize + "byte");

                // 调用服务
                Object service = registeredService.get(rpcRequestBody.getInterfaceName());
                Method method = service.getClass().getMethod(rpcRequestBody.getMethodName(), rpcRequestBody.getParamTypes());
                Object returnObject = method.invoke(service, rpcRequestBody.getParameters());

                // 1、将returnObject编码成bytes[]即变成了返回编码【codec层】
                byte serializationType = reqHeader.getSerialization();
                byte msgType = (byte) MsgType.RESPONSE.ordinal(); // 注意这里是响应类型

                // 响应消息头
                MsgHeader respHeader = rpcRequest.getHeader();
                respHeader.setMagic(ProtocolConstants.MAGIC);
                respHeader.setVersion(VERSION);
                respHeader.setSerialization(serializationType); // 配置文件读取方式，暂时使用hessian
                respHeader.setMsgType(msgType);
                respHeader.setStatus((byte) 0x1);

                // 响应消息体
                RpcResponseBody rpcResponseBody = RpcResponseBody.builder()
                        .retObject(returnObject)
                        .build();

                // 序列化
                byte[] bytes = RpcEncoder.encode(rpcResponseBody, serializationType);

                // 2、将返回编码作为body，加上header，生成RpcResponse协议【protocol层】
                RpcProtocol rpcResponse = new RpcProtocol();
                rpcResponse.setHeader(respHeader);
                rpcResponse.setBody(bytes);

                // 3、发送【transfer层】
                objectOutputStream.writeObject(rpcResponse);
                objectOutputStream.flush();
            }

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
