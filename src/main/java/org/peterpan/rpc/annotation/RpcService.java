package org.peterpan.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 服务提供方
 *
 * @RpcService 注解上的 @Component 注解表示该注解被标记的类会被 Spring 框架自动识别为一个组件，并将其实例化为一个 Bean
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

   // 指定实现方,默认为实现接口中第一个
   Class<?> serviceInterface() default void.class;

   String serviceVersion() default "1.0";
}
