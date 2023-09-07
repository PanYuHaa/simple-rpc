package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description NIO 并发测试
 */
@Slf4j
public class ConcurrentNIOClient {
   public static void main(String[] args) {
      int numClients = 10000; // 客户端数量

      int corePoolSize = 5; // 5
      int maximumPoolSize = 50; // 50
      long keepAliveTime = 60;
      BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(10000); // 100
      ThreadFactory threadFactory = Executors.defaultThreadFactory();
      // 创建自定义的拒绝策略实例
      ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
              corePoolSize,
              maximumPoolSize,
              keepAliveTime,
              TimeUnit.SECONDS,
              workingQueue,
              threadFactory
      );

      long startTime = System.nanoTime();

      for (int i = 0; i < numClients; i++) {
         threadPool.submit(new ClientTask(i));
      }

      long endTime = System.nanoTime();
      long executionTime = (endTime - startTime) / 1_000_000; // 计算执行时间(毫秒为单位)
      log.info("done, executionTime={}ms", executionTime);
      threadPool.shutdown();
   }

   static class ClientTask implements Runnable {
      private final int clientId;

      public ClientTask(int clientId) {
         this.clientId = clientId;
      }

      @Override
      public void run() {
         try {
            // 创建SocketChannel并连接服务器
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 8080));

            // 发送数据到服务器
            String message = "Hello from Client " + clientId;
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);

            // 关闭客户端
            socketChannel.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
}
