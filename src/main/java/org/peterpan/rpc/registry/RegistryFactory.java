package org.peterpan.rpc.registry;


import org.peterpan.rpc.registry.impl.RedisRegistry;
import org.peterpan.rpc.registry.impl.ZookeeperRegistry;
import org.peterpan.rpc.spi.ExtensionLoader;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 注册工厂
 *
 * 使用双重校验来创建单例。这里使用了工厂设计模式，工厂设计模式多伴随还有接口的设计(RegistryService)
 */
public class RegistryFactory {

   public static IRegistryService get(String registryService) throws Exception {
      return ExtensionLoader.getInstance().get(registryService);
   }

   public static void init() throws Exception {
      ExtensionLoader.getInstance().loadExtension(IRegistryService.class);
   }

}

