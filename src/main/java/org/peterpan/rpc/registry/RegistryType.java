package org.peterpan.rpc.registry;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description
 */
public enum RegistryType {
   ZOOKEEPER,
   REDIS;

   public static RegistryType toRegistry(String registerType) {
      for (RegistryType value : values()) {
         if (value.toString().equals(registerType)) {
            return value;
         }
      }
      return null;
   }
}
