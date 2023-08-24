package org.peterpan.rpc.router.tolerant;

import org.peterpan.rpc.router.tolerant.impl.FailFastFaultTolerantHandler;
import org.peterpan.rpc.router.tolerant.impl.FailoverFaultTolerantHandler;
import org.peterpan.rpc.router.tolerant.impl.FailsafeFaultTolerantHandler;

import java.util.HashMap;
import java.util.Map;


/**
 * @author PeterPan
 * @date 2023/7/17
 * @description 集群容错工厂
 */
public class FaultTolerantFactory {

   private static Map<FaultTolerantType, IFaultTolerantHandler> faultTolerantStrategyMap = new HashMap<>();
   static {
      faultTolerantStrategyMap.put(FaultTolerantType.FailFast, new FailFastFaultTolerantHandler());
      faultTolerantStrategyMap.put(FaultTolerantType.Failover, new FailoverFaultTolerantHandler());
      faultTolerantStrategyMap.put(FaultTolerantType.Failsafe, new FailsafeFaultTolerantHandler());
   }

   public static IFaultTolerantHandler get(FaultTolerantType faultTolerantType){
      return faultTolerantStrategyMap.get(faultTolerantType);
   }
}

