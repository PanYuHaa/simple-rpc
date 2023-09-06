package org.peterpan.rpc.consumer;

import org.peterpan.rpc.annotation.RpcReference;
import org.peterpan.rpc.config.RpcProperties;
import org.peterpan.rpc.registry.RegistryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 消费方后置处理器
 *
 * 执行流程为setBeanClassLoader() -> setApplicationContext() -> setEnvironment() -> postProcessBeanFactory()
 * rpcRefBeanDefinitions 这个 map获取【(K)bean名字】与【(V)bean描述信息】的对应关系
 */
@Slf4j // 自动生成日志变量 log
@Configuration // 声明为配置类
public class ConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor, EnvironmentAware {

   private ApplicationContext context; // 应用程序上下文
   private ClassLoader classLoader; // 类加载器
   private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>(); // 保存解析出的 RPC 引用 Bean 的定义
   RpcProperties rpcProperties; // RPC 配置属性

   @Override
   public void setBeanClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader; // 设置类加载器
   }

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      this.context = applicationContext; // 设置应用程序上下文
   }

   @Override
   public void setEnvironment(Environment environment) {
      String prefix = "rpc."; // 属性前缀
      String registerAddr = environment.getProperty(prefix + "register-addr"); // 获取注册地址属性值
      String registerType = environment.getProperty(prefix + "register-type"); // 获取注册类型属性值
      String registerPsw = environment.getProperty(prefix + "register-psw"); // 获取注册密码属性值
      if (ObjectUtils.isEmpty(registerType)) {
         registerType = "ZOOKEEPER"; // 默认注册类型为 ZOOKEEPER
      }
      if (RegistryType.toRegistry(registerType) == null) {
         throw new IllegalArgumentException("Properties in registerType fail."); // 注册类型无效，抛出异常
      }
      RpcProperties properties = RpcProperties.getInstance(); // 获取 RpcProperties 单例实例
      properties.setRegisterAddr(registerAddr); // 设置注册地址
      properties.setRegisterType(registerType); // 设置注册类型
      properties.setRegisterPsw(registerPsw); // 设置注册密码
      rpcProperties = properties; // 保存 RpcProperties 实例
   }

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      // 每个 Bean 都由一个 BeanDefinition 对象来描述其类型、属性、作用域、生命周期等信息
      for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) { // 遍历所有的 BeanDefinition
         BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName); // 获取 BeanDefinition
         String beanClassName = beanDefinition.getBeanClassName(); // 获取 Bean 的类名
         if (beanClassName != null) {
            Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader); // 根据类名解析出 Class 对象
            ReflectionUtils.doWithFields(clazz, this::parseRpcReference); // 遍历字段并解析 RpcReference 注解
         }
      }

      BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory; // BeanDefinitionRegistry 是注册和管理 BeanDefinition 的类
      this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
         if (context.containsBean(beanName)) {
            throw new IllegalArgumentException("spring context already has a bean named " + beanName);
         }
         registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName)); // 注册 RpcReferenceBean 的 BeanDefinition
         log.info("Registered RpcReferenceBean {} successfully.", beanName);
      });
   }

   private void parseRpcReference(Field field) {
      RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class); // 获取字段上的 RpcReference 注解
      if (annotation != null) {
         String address = rpcProperties.getRegisterAddr(); // 获取注册地址
         String type = rpcProperties.getRegisterType(); // 获取注册类型
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class); // 以 RpcReferenceBean 为模版生成所有被 @RpcReference 标记的bean信息
         builder.setInitMethodName("init"); // 设置初始化方法为 "init"
         builder.addPropertyValue("interfaceClass", field.getType()); // 设置接口类型
         builder.addPropertyValue("serviceVersion", annotation.serviceVersion()); // 设置服务版本
         builder.addPropertyValue("registryType", type); // 设置注册类型
         builder.addPropertyValue("registryAddr", address); // 设置注册地址
         builder.addPropertyValue("timeout", annotation.timeout()); // 设置超时时间
         builder.addPropertyValue("loadBalancerType", annotation.loadBalancerType().toString()); // 设置负载均衡类型
         builder.addPropertyValue("faultTolerantType", annotation.faultTolerantType().toString()); // 设置容错类型
         builder.addPropertyValue("retryCount", annotation.retryCount()); // 设置重试次数
         BeanDefinition beanDefinition = builder.getBeanDefinition(); // 获取 BeanDefinition
         rpcRefBeanDefinitions.put(field.getName(), beanDefinition); // 将解析出的 BeanDefinition 放入 Map 中
      }
   }

}