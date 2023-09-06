package org.peterpan.rpc.tolerant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 集群容错工厂
 */
public class FaultTolerantFactory {

   private static Map<FaultTolerantFactory, IFaultTolerantStrategy> faultTolerantStrategyMap = new HashMap<>();
   static {

   }

   public static IFaultTolerantStrategy get(FaultTolerantFactory faultTolerantType){
      return faultTolerantStrategyMap.get(faultTolerantType);
   }
}
