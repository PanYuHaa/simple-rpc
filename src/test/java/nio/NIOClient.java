package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author PeterPan
 * @date 2023/9/6
 * @description nio c端测试
 */
public class NIOClient {
    public static void main(String[] args) {
        try {
            // 创建SocketChannel并连接服务器
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 8080));

            // 发送数据到服务器
            String message = "Hello, NIO Server!";
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);

            // 关闭客户端
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
