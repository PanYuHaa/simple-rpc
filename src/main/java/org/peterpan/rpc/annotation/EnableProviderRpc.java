package org.peterpan.rpc.annotation;

import org.peterpan.rpc.provider.ProviderPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 开启服务提供方自动装配
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ProviderPostProcessor.class)
public @interface EnableProviderRpc {
}
