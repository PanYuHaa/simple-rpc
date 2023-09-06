package org.peterpan.rpc.annotation;

import org.peterpan.rpc.consumer.ConsumerPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 开启消费方自动装配注解
 *
 * @Target 注解用于指定自定义注解可以应用的目标元素类型。在这里，ElementType.TYPE 表示该自定义注解可以应用于类、接口或枚举类型
 * @Retention 注解用于指定自定义注解的保留策略，即注解在何时可用。RetentionPolicy.RUNTIME 表示注解将在运行时保留，可以通过反射等方式读取注解信息。
 * @Import 注解用于导入其他的配置类或组件类。在这里，ConsumerPostProcessor.class 是要导入的配置类。它表示在使用了 @EnableConsumerRpc 注解的类中，将会导入 ConsumerPostProcessor 类，用于执行一些后置处理逻辑。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ConsumerPostProcessor.class)
public @interface EnableConsumerRpc {
}
