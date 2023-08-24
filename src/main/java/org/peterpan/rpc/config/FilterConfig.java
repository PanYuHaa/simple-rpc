package org.peterpan.rpc.config;


import lombok.SneakyThrows;
import org.peterpan.rpc.filter.*;
import org.peterpan.rpc.spi.ExtensionLoader;

import java.io.IOException;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description
 */
public class FilterConfig {


    private static FilterChain serviceBeforeFilterChain = new FilterChain();
    private static FilterChain serviceAfterFilterChain = new FilterChain();
    private static FilterChain clientBeforeFilterChain = new FilterChain();
    private static FilterChain clientAfterFilterChain = new FilterChain();

    @SneakyThrows
    public static void initServiceFilter(){
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(IServiceAfterFilter.class);
        extensionLoader.loadExtension(IServiceBeforeFilter.class);
        serviceBeforeFilterChain.addFilter(extensionLoader.gets(IServiceBeforeFilter.class));
        serviceAfterFilterChain.addFilter(extensionLoader.gets(IServiceAfterFilter.class));
    }
    public static void initClientFilter() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(IClientAfterFilter.class);
        extensionLoader.loadExtension(IClientBeforeFilter.class);
        clientBeforeFilterChain.addFilter(extensionLoader.gets(IClientBeforeFilter.class));
        clientAfterFilterChain.addFilter(extensionLoader.gets(IClientAfterFilter.class));
    }

    public static FilterChain getServiceBeforeFilterChain(){
        return serviceBeforeFilterChain;
    }
    public static FilterChain getServiceAfterFilterChain(){
        return serviceAfterFilterChain;
    }
    public static FilterChain getClientBeforeFilterChain(){
        return clientBeforeFilterChain;
    }
    public static FilterChain getClientAfterFilterChain(){
        return clientAfterFilterChain;
    }
}
