package rpc.peterpan.com.infrastructure.tolerant;

import java.util.HashMap;
import java.util.Map;


/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 集群容错工厂
 */
public class FaultTolerantFactory {

   private static Map<FaultTolerantType, IFaultTolerantStrategy> faultTolerantStrategyMap = new HashMap<>();
   static {

   }

   public static IFaultTolerantStrategy get(FaultTolerantType faultTolerantType){
      return faultTolerantStrategyMap.get(faultTolerantType);
   }
}

