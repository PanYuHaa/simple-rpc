package org.peterpan.rpc.ConsistentHashingDemo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author PeterPan
 * @date 2023/7/21
 * @description 测试一致性hash
 */
public class ConsistentHashingDemo {

   // 服务器列表
   private final List<String> servers;

   // 虚拟节点数
   private final int virtualNodes;

   // 哈希环
   private final TreeMap<Long, String> hashRing = new TreeMap<>();

   public ConsistentHashingDemo(List<String> servers, int virtualNodes) {
      this.servers = servers;
      this.virtualNodes = virtualNodes;

      // 构建哈希环
      for (String server : servers) {
         for (int i = 0; i < virtualNodes; i++) {
            byte[] digest = md5(server + ":" + i);
            long hash = hash(digest, 0);
            hashRing.put(hash, server);
         }
      }
   }

   /**
    * 获取指定 key 所对应的服务器节点
    *
    * @param key 键值
    * @return 服务器节点
    */
   public String getServer(String key) {
      byte[] digest = md5(key);
      long hash = hash(digest, 0);

      Map.Entry<Long, String> entry = hashRing.ceilingEntry(hash);
      if (entry == null) {
         entry = hashRing.firstEntry();
      }
      return entry.getValue();
   }

   /**
    * 计算 MD5 哈希值
    *
    * @param text 输入字符串
    * @return 哈希值
    */
   private byte[] md5(String text) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         return md.digest(text.getBytes(StandardCharsets.UTF_8));
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * 将字节数组转换成 long 类型
    *
    * @param digest 字节数组
    * @param startIndex 开始位置
    * @return long 类型结果
    */
   private long hash(byte[] digest, int startIndex) {
      long result = 0;
      for (int i = startIndex; i < startIndex + 8; i++) {
         result = (result << 8) | (digest[i] & 0xFF);
      }
      return result;
   }

   public static void main(String[] args) {
      t();
   }

   public static void test_ring(){
      // 初始化服务器列表
      List<String> servers = Arrays.asList("192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4");

      // 构建一致性哈希环
      ConsistentHashingDemo demo = new ConsistentHashingDemo(servers, 10);

      // 计算指定键值的服务器节点
      String server1 = demo.getServer("key");
      System.out.println("key1 的服务器节点为：" + server1);

      String server2 = demo.getServer("key2");
      System.out.println("key2 的服务器节点为：" + server2);
   }
   public static void t(){
      TreeMap<Integer, String> ring = new TreeMap<>();
      ring.put(1,"1");
      ring.put(10,"10");
      ring.put(100,"100");
      // 大于key最大的话会返回空，这时直接返回第一个节点就行
      Map.Entry<Integer, String> integerStringEntry = ring.ceilingEntry(101);
      System.out.println(integerStringEntry);
   }
}

