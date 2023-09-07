package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description nio server测试
 */
@Slf4j
public class NIOServer {
   public static void main(String[] args) {
      try {
         // 创建ServerSocketChannel并绑定端口
         ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.socket().bind(new InetSocketAddress(8080));
         serverSocketChannel.configureBlocking(false);

         // 创建Selector
         Selector selector = Selector.open();
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

         System.out.println("Server started on port 8080...");

         while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
               SelectionKey key = keyIterator.next();

               if (key.isAcceptable()) {
                  // 处理新的连接请求
                  SocketChannel clientChannel = serverSocketChannel.accept();
                  clientChannel.configureBlocking(false);
                  clientChannel.register(selector, SelectionKey.OP_READ);
               } else if (key.isReadable()) {
                  // 处理可读事件
                  SocketChannel clientChannel = (SocketChannel) key.channel();
                  ByteBuffer buffer = ByteBuffer.allocate(1024);
                  int bytesRead = clientChannel.read(buffer);
                  if (bytesRead == -1) {
                     clientChannel.close();
                  } else if (bytesRead > 0) {
                     buffer.flip();
                     byte[] data = new byte[bytesRead];
                     buffer.get(data);
                     String message = new String(data);
                     log.info("Received message: " + message);
                  }
               }

               keyIterator.remove();
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
