package rpc.peterpan.com.core.server;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import rpc.peterpan.com.core.codec.RpcDecoder;
import rpc.peterpan.com.core.codec.RpcEncoder;
import rpc.peterpan.com.common.MsgType;
import rpc.peterpan.com.common.ProtocolConstants;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.body.RpcRequestBody;
import rpc.peterpan.com.core.protocol.body.RpcResponseBody;
import rpc.peterpan.com.core.protocol.header.MsgHeader;
import rpc.peterpan.com.util.redisKey.RpcServiceNameBuilder;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;

import static java.lang.Thread.sleep;
import static rpc.peterpan.com.common.ProtocolConstants.MAGIC;
import static rpc.peterpan.com.common.ProtocolConstants.VERSION;
import static rpc.peterpan.com.common.StatusConstants.NORMAL;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description rpc服务端线程执行
 */
@Slf4j
@Data
public class RpcServerWorker implements Runnable {

    private Socket socket;
    private HashMap<String, Object> registeredService;

    public RpcServerWorker(Socket socket, HashMap<String, Object> registeredService) {
        this.socket = socket;
        this.registeredService = registeredService;
    }

    @SneakyThrows
    @Override
    public void run() {
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            // 1、Transfer层获取到RpcRequest消息【transfer层】
            RpcProtocol rpcRequest = (RpcProtocol) objectInputStream.readObject();
            MsgHeader reqHeader = rpcRequest.getHeader();

            // 2、解析版本号，并判断【protocol层】
            if (reqHeader.getMagic() == MAGIC) {

                // 3、将rpcRequest中的body部分解码出来变成RpcRequestBody【codec层】
                long startTime = System.nanoTime();

                byte[] body = rpcRequest.getBody();
                RpcRequestBody rpcRequestBody = (RpcRequestBody) RpcDecoder.decode(body, reqHeader.getSerialization(), reqHeader.getMsgType());

                long endTime = System.nanoTime();
                long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
                int byteSize = body.length;
//                log.info("[{}_{}${}] - 反序列化执行时间={}ms, 数据大小={}byte", rpcRequestBody.getInterfaceName(), rpcRequestBody.getServiceVersion(), rpcRequestBody.getMethodName(), executionTime, byteSize);

                // 调用服务
                Object returnObject = handle(rpcRequestBody);

                // 1、将returnObject编码成bytes[]即变成了返回编码【codec层】
                byte serializationType = reqHeader.getSerialization();
                byte msgType = (byte) MsgType.RESPONSE.ordinal(); // 注意这里是响应类型

                // 响应消息头
                MsgHeader respHeader = rpcRequest.getHeader();
                respHeader.setMagic(ProtocolConstants.MAGIC);
                respHeader.setVersion(VERSION);
                respHeader.setSerialization(serializationType); // 配置文件读取方式，暂时使用hessian
                respHeader.setMsgType(msgType);
                respHeader.setStatus((byte) NORMAL);

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
        } finally {
            objectInputStream.close();
            sleep(5000);
            objectOutputStream.close(); // 这里的释放资源要间隔一段时间，因为这个输入不产生返回值我无法保证对方读取完整数据，延迟5s关闭
        }
    }

    /**
     * 处理具体的 RPC 请求
     */
    private Object handle(RpcRequestBody request) throws Throwable {
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getInterfaceName(), request.getServiceVersion());
        // 获取服务信息
        Object serviceBean = registeredService.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getInterfaceName(), request.getMethodName()));
        }

        // 获取服务提供方信息并且创建
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParamTypes();
        Object[] parameters = request.getParameters();

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        // 调用方法并返回结果
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
}
