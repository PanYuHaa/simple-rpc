package org.peterpan.rpc.annotation;

import org.peterpan.rpc.registry.loadbalancer.LoadBalancerType;
import org.peterpan.rpc.tolerant.FaultTolerantType;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 服务消费方,在使用该注解后还应添加这个注解忽视错误
 * @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
 * 尝试使用@Resource但是会注入不了ioc。其他方式也尝试过，水平能力有限
 *
 * @RpcReference 注解是用于标记需要进行远程服务引用的类，通过 @Autowired 注解配合使用，可以实现自动注入远程服务。这样，在使用该类时，相关的远程服务引用就已经被自动注入了
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Autowired
public @interface RpcReference {

   String serviceVersion() default "1.0";

   long timeout() default 5000;

   LoadBalancerType loadBalancerType() default LoadBalancerType.RoundRobin;

   FaultTolerantType faultTolerantType() default FaultTolerantType.Failover;

   long retryCount() default 3;
}
