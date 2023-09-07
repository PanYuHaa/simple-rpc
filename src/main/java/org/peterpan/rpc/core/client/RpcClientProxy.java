package org.peterpan.rpc.core.client;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.common.*;
import org.peterpan.rpc.config.FilterConfig;
import org.peterpan.rpc.config.RpcConfig;
import org.peterpan.rpc.core.codec.RpcDecoder;
import org.peterpan.rpc.core.codec.RpcEncoder;
import org.peterpan.rpc.core.codec.serialization.SerializationFactory;
import org.peterpan.rpc.core.codec.serialization.SerializationTypeEnum;
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
import org.springframework.util.ObjectUtils;

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

//        // 3、发送RpcRequest，获得RpcResponse【transfer层】
//        // 与注册中心交互
//        String serviceKey = RpcServiceNameBuilder.buildServiceKey(rpcRequestBody.getInterfaceName(), rpcRequestBody.getServiceVersion());
//        Object[] params = rpcRequestBody.getParameters();
//        // 计算哈希
//        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
//        // 根据服务key以及哈希获取服务提供方节点
//        ServiceMeta curServiceMeta = registryCenter.
//                discovery(serviceKey, invokerHashCode, loadBalancerType);
//        // 供故障转移使用
//        List<ServiceMeta> serviceMetas = registryCenter.discoveries(serviceKey);
//        IFaultTolerantHandler faultTolerantHandler = FaultTolerantFactory.get(faultTolerantType);
//        int count = 1;
//        int retryCount = this.retryCount;
//        RpcClientTransfer rpcClient = new RpcClientTransfer();
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        while (count < retryCount) {
//            ServiceMeta finalCurServiceMeta = curServiceMeta;
//            Future<RpcProtocol> future = executorService.submit(() -> rpcClient.sendRequest(rpcRequest, finalCurServiceMeta));
//            try {
//                RpcProtocol rpcResponse = future.get(timeout, TimeUnit.MILLISECONDS);
//
//                // 4、解析RpcResponse，也就是在解析rpc协议【protocol层】
//                MsgHeader respHeader = rpcResponse.getHeader(); // 来自于响应的header
//                byte[] body = rpcResponse.getBody();
//                if (respHeader.getMagic() == ProtocolConstants.MAGIC) {
//                    // 将RpcResponse的body中的返回编码，解码成我们需要的对象Object并返回【codec层】
//                    RpcResponseBody rpcResponseBody = (RpcResponseBody) RpcDecoder.decode(body, respHeader.getSerialization(), respHeader.getMsgType());
//                    Object retObject = rpcResponseBody.getRetObject();
//                    log.info("requestID={}, rpc 调用成功, serviceKey={}, interface={}", respHeader.getRequestId(), serviceKey, rpcRequestBody.getMethodName());
//                    try {
//                        FilterConfig.getClientAfterFilterChain().doFilter(filterData);
//                    }catch (Throwable e){
//                        throw e;
//                    }
//                    return retObject;
//                }
//            } catch (TimeoutException e) {
//                // 超时处理逻辑
//                future.cancel(true); // 尝试取消任务
//                String errorMsg = "RPC调用超时"; // 自定义错误信息
//                FaultTolerantContext ftCtx = faultTolerantHandler.tolerant(
//                        FaultTolerantContext.builder()
//                                .serviceKey(serviceKey)
//                                .methodName(rpcRequestBody.getMethodName())
//                                .errorMsg(errorMsg)
//                                .serviceMeta(curServiceMeta)
//                                .serviceMetas(serviceMetas)
//                                .count(count)
//                                .retryCount(retryCount)
//                                .requestId(reqHeader.getRequestId())
//                                .build()
//                        );
//                if (ftCtx == null) {
//                    return null;
//                }
//                count = ftCtx.getCount();
//            } catch (ExecutionException e) {
//                // 处理Future内部的异常
//                String errorMsg = "RPC调用失败:" + e.getCause().getMessage(); // 自定义错误信息
//                FaultTolerantContext ftCtx = faultTolerantHandler.tolerant(
//                        FaultTolerantContext.builder()
//                                .serviceKey(serviceKey)
//                                .methodName(rpcRequestBody.getMethodName())
//                                .errorMsg(errorMsg)
//                                .serviceMeta(curServiceMeta)
//                                .serviceMetas(serviceMetas)
//                                .count(count)
//                                .retryCount(retryCount)
//                                .requestId(reqHeader.getRequestId())
//                                .build()
//                );
//                if (ftCtx == null) {
//                    return null;
//                }
//                count = ftCtx.getCount();
//            }
//
//        }
//        throw new RuntimeException("requestID=" + reqHeader.getRequestId() + ", RPC调用失败，超过最大重试次数=" + retryCount + ", serviceKey=" + serviceKey + ", interface=" + rpcRequestBody.getMethodName());
//    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProtocol<RpcRequestBody> protocol = new RpcProtocol<>();
        // 构建消息头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        // 构建请求体
        RpcRequestBody request = new RpcRequestBody();
        request.setServiceVersion(this.serviceVersion);
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParamsTypes(method.getParameterTypes());
        request.setParams(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        protocol.setBody(request);

        RpcClientTransfer rpcConsumer = new RpcClientTransfer();
        // 处理返回数据
        RpcFuture<RpcResponseBody> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);

        RpcRequestHolder.REQUEST_MAP.put(requestId, future);

        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getInterfaceName(), request.getServiceVersion());
        Object[] params = request.getParams();
        // 计算哈希
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        // 根据服务key以及哈希获取服务提供方节点
        ServiceMeta curServiceMeta = registryCenter.
                discovery(serviceKey, invokerHashCode, loadBalancerType);
        // 供故障转移使用
        List<ServiceMeta> serviceMetas = this.registryCenter.discoveries(serviceKey);
        long count = 1;
        long retryCount = this.retryCount;
        while (count <= retryCount) {
            try {
                rpcConsumer.sendRequest(protocol, curServiceMeta);
                RpcResponseBody rpcResponse = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);
//                log.info("rpc 调用成功, serviceKey: {}", serviceKey);
                return rpcResponse.getData();
            } catch (Exception e) {
                switch (faultTolerantType) {
                    // 快速失败
                    case FailFast:
                        log.warn("rpc 调用失败,触发 FailFast 策略");
                        break;
                    // 故障转移
                    case Failover:
                        log.warn("rpc 调用失败,第{}次重试", count);
                        count++;
                        serviceMetas.remove(curServiceMeta); // 直接先删除当前节点
                        if (!ObjectUtils.isEmpty(serviceMetas)) {
                            curServiceMeta = serviceMetas.get(0);
                        } else {
                            log.warn("rpc 调用失败,无服务可用 serviceKey: {}", serviceKey);
                            count = retryCount;
                        }
                        break;
                    // 忽视这次错误
                    case Failsafe:
                        return null;
                }
            }
        }

        throw new RuntimeException("RPC调用失败，超过最大重试次数：" + retryCount);
    }
}

