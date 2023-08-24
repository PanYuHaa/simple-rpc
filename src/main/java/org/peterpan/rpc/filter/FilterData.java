package org.peterpan.rpc.filter;


import org.peterpan.rpc.core.protocol.body.RpcRequestBody;
import org.peterpan.rpc.core.protocol.body.RpcResponseBody;

import java.util.Arrays;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description 上下文数据
 */
public class FilterData {


    private String serviceVersion;
    private long timeout;
    private long retryCount;
    private String interfaceName;
    private String methodName;
    private Object[] args;
    private Map<String,Object> serviceAttachments;
    private Map<String,Object> clientAttachments;
    private RpcResponseBody data; // 执行业务逻辑后的数据(RpcResponse暂时与xhy不同，因为还需要改动其他地方)

    public FilterData(RpcRequestBody request) {
        this.args = request.getParameters();
        this.interfaceName = request.getInterfaceName();
        this.methodName = request.getMethodName();
        this.serviceVersion = request.getServiceVersion();
        this.serviceAttachments = request.getServiceAttachments();
        this.clientAttachments = request.getClientAttachments();
    }
    public FilterData(){

    }

    public RpcResponseBody getData() {
        return data;
    }

    public void setData(RpcResponseBody data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "调用: Class: " + interfaceName + " Method: " + methodName + " args: " + Arrays.toString(args) +" Version: " + serviceVersion
                +" Timeout: " + timeout +" ServiceAttachments: " + serviceAttachments +
                " ClientAttachments: " + clientAttachments;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(long retryCount) {
        this.retryCount = retryCount;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, Object> getServiceAttachments() {
        return serviceAttachments;
    }

    public void setServiceAttachments(Map<String, Object> serviceAttachments) {
        this.serviceAttachments = serviceAttachments;
    }

    public Map<String, Object> getClientAttachments() {
        return clientAttachments;
    }

    public void setClientAttachments(Map<String, Object> clientAttachments) {
        this.clientAttachments = clientAttachments;
    }
}
