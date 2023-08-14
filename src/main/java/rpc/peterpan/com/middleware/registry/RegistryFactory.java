package rpc.peterpan.com.middleware.registry;


import rpc.peterpan.com.middleware.registry.impl.RedisRegistry;
import rpc.peterpan.com.middleware.registry.impl.ZookeeperRegistry;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 注册工厂
 *
 * 使用双重校验来创建单例。这里使用了工厂设计模式，工厂设计模式多伴随还有接口的设计(RegistryService)
 */
public class RegistryFactory {

   private static volatile IRegistryService IRegistryService;


   public static IRegistryService getInstance(RegistryType type) throws Exception {

      if (null == IRegistryService) {
         synchronized (RegistryFactory.class) {
            if (null == IRegistryService) {
               switch (type) {
                  case ZOOKEEPER:
                     IRegistryService = new ZookeeperRegistry();
                     break;
                  case REDIS:
                     IRegistryService = new RedisRegistry();
               }
            }
         }
      }
      return IRegistryService;
   }

}

