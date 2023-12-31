package org.peterpan.rpc.core.client;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.MsgType;
import org.peterpan.rpc.common.ProtocolConstants;
import org.peterpan.rpc.common.ServiceMeta;
import org.peterpan.rpc.common.StatusConstants;
import org.peterpan.rpc.config.FilterConfig;
import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.core.codec.RpcDecoder;
import org.peterpan.rpc.core.codec.RpcEncoder;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.core.protocol.RpcProtocol;
import org.peterpan.rpc.core.protocol.body.RpcRequestBody;
import org.peterpan.rpc.core.protocol.body.RpcResponseBody;
import org.peterpan.rpc.core.protocol.header.MsgHeader;
import org.peterpan.rpc.core.transfer.RpcClientTransfer;
import org.peterpan.rpc.filter.FilterChain;
import org.peterpan.rpc.filter.FilterData;
import org.peterpan.rpc.registry.IRegistryService;
import org.peterpan.rpc.registry.RegistryFactory;
import org.peterpan.rpc.router.tolerant.FaultTolerantContext;
import org.peterpan.rpc.router.tolerant.FaultTolerantFactory;
import org.peterpan.rpc.router.tolerant.FaultTolerantType;
import org.peterpan.rpc.router.tolerant.IFaultTolerantHandler;
import org.peterpan.rpc.util.RequestIdGenerator;
import org.peterpan.rpc.util.redisKey.RpcServiceNameBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description 客户端的代理类
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private RpcConfig rpcConfig; // Rpc配置中心
    private IRegistryService registryCenter; // 注册中心
    private String serviceVersion; // 服务版本
    private String loadBalancerType; // 负载均衡类型
    private FaultTolerantType faultTolerantType; // 容错类型
    private int retryCount; // 重试次数
    private long timeout; // 超时控制

    public RpcClientProxy(RpcConfig rpcConfig) throws Exception {
        // 加载组件
        RegistryFactory.init();
        SerializationFactory.init();
        FilterConfig.initClientFilter();

        this.rpcConfig = rpcConfig;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz, String serviceVersion, String loadBalancerType, String faultTolerantType, long timeout) throws Exception {
        this.registryCenter = RegistryFactory.get(RpcConfig.getInstance().getRegisterType());
        this.serviceVersion = serviceVersion;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = FaultTolerantType.toFaultTolerant(faultTolerantType);
        this.retryCount = Integer.valueOf(rpcConfig.getRetryCount());
        this.timeout = timeout;
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 1、将调用所需信息编码成bytes[]，即有了调用编码【codec层】
        long startTime = System.nanoTime();

        byte serializationType = rpcConfig.getSerializationByte();
        byte msgType = (byte) MsgType.REQUEST.ordinal();

        // 构建消息头
        MsgHeader reqHeader = new MsgHeader();
        reqHeader.setMagic(ProtocolConstants.MAGIC);
        reqHeader.setVersion(ProtocolConstants.VERSION);
        reqHeader.setSerialization(serializationType); // 配置文件读取方
        reqHeader.setMsgType(msgType); // 注意这里是请求REQUEST
        reqHeader.setStatus((byte) StatusConstants.NORMAL);
        reqHeader.setRequestId(RequestIdGenerator.generateRequestId());

        // 构建消息体
        RpcRequestBody rpcRequestBody = RpcRequestBody.builder()
                .serviceVersion(serviceVersion)
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .parameters(args)
                .build();

        // 序列化
        byte[] bytes = RpcEncoder.encode(rpcRequestBody, serializationType);

        // 拦截器的上下文
        final FilterData filterData = new FilterData(rpcRequestBody);
        try {
            FilterConfig.getClientBeforeFilterChain().doFilter(filterData);
        }catch (Throwable e){
            throw e;
        }

        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
        int byteSize = bytes.length;
//        log.info("requestID={}, [执行{}序列化方式] - [{}_{}${}] - 序列化执行时间={}ms, 数据大小={}byte", reqHeader.getRequestId(), SerializationTypeEnum.findByType(serializationType), method.getDeclaringClass().getName(), serviceVersion, method.getName(), executionTime, byteSize);

        // 2、创建RPC协议，将Header、Body的内容设置好（Body中存放调用编码）【protocol层】
        RpcProtocol rpcRequest = new RpcProtocol();
        rpcRequest.setHeader(reqHeader);
        rpcRequest.setBody(bytes);

        // 3、发送RpcRequest，获得RpcResponse【transfer层】
        // 与注册中心交互
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(rpcRequestBody.getInterfaceName(), rpcRequestBody.getServiceVersion());
        Object[] params = rpcRequestBody.getParameters();
        // 计算哈希
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        // 根据服务key以及哈希获取服务提供方节点
        ServiceMeta curServiceMeta = registryCenter.
                discovery(serviceKey, invokerHashCode, loadBalancerType);
        // 供故障转移使用
        List<ServiceMeta> serviceMetas = registryCenter.discoveries(serviceKey);
        IFaultTolerantHandler faultTolerantHandler = FaultTolerantFactory.get(faultTolerantType);
        int count = 1;
        int retryCount = this.retryCount;
        RpcClientTransfer rpcClient = new RpcClientTransfer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        while (count < retryCount) {
            ServiceMeta finalCurServiceMeta = curServiceMeta;
            Future<RpcProtocol> future = executorService.submit(() -> rpcClient.sendRequest(rpcRequest, finalCurServiceMeta));
            try {
                RpcProtocol rpcResponse = future.get(timeout, TimeUnit.MILLISECONDS);

                // 4、解析RpcResponse，也就是在解析rpc协议【protocol层】
                MsgHeader respHeader = rpcResponse.getHeader(); // 来自于响应的header
                byte[] body = rpcResponse.getBody();
                if (respHeader.getMagic() == ProtocolConstants.MAGIC) {
                    // 将RpcResponse的body中的返回编码，解码成我们需要的对象Object并返回【codec层】
                    RpcResponseBody rpcResponseBody = (RpcResponseBody) RpcDecoder.decode(body, respHeader.getSerialization(), respHeader.getMsgType());
                    Object retObject = rpcResponseBody.getRetObject();
                    log.info("requestID={}, rpc 调用成功, serviceKey={}, interface={}", respHeader.getRequestId(), serviceKey, rpcRequestBody.getMethodName());
                    try {
                        FilterConfig.getClientAfterFilterChain().doFilter(filterData);
                    }catch (Throwable e){
                        throw e;
                    }
                    return retObject;
                }
            } catch (TimeoutException e) {
                // 超时处理逻辑
                future.cancel(true); // 尝试取消任务
                String errorMsg = "RPC调用超时"; // 自定义错误信息
                FaultTolerantContext ftCtx = faultTolerantHandler.tolerant(
                        FaultTolerantContext.builder()
                                .serviceKey(serviceKey)
                                .methodName(rpcRequestBody.getMethodName())
                                .errorMsg(errorMsg)
                                .serviceMeta(curServiceMeta)
                                .serviceMetas(serviceMetas)
                                .count(count)
                                .retryCount(retryCount)
                                .requestId(reqHeader.getRequestId())
                                .build()
                        );
                if (ftCtx == null) {
                    return null;
                }
                count = ftCtx.getCount();
            } catch (ExecutionException e) {
                // 处理Future内部的异常
                String errorMsg = "RPC调用失败:" + e.getCause().getMessage(); // 自定义错误信息
                FaultTolerantContext ftCtx = faultTolerantHandler.tolerant(
                        FaultTolerantContext.builder()
                                .serviceKey(serviceKey)
                                .methodName(rpcRequestBody.getMethodName())
                                .errorMsg(errorMsg)
                                .serviceMeta(curServiceMeta)
                                .serviceMetas(serviceMetas)
                                .count(count)
                                .retryCount(retryCount)
                                .requestId(reqHeader.getRequestId())
                                .build()
                );
                if (ftCtx == null) {
                    return null;
                }
                count = ftCtx.getCount();
            }

        }
        throw new RuntimeException("requestID=" + reqHeader.getRequestId() + ", RPC调用失败，超过最大重试次数=" + retryCount + ", serviceKey=" + serviceKey + ", interface=" + rpcRequestBody.getMethodName());
    }
}

