package rpc.peterpan.com.core.transfer.RejectedExecutionHandler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rpc.peterpan.com.common.StatusConstants;
import rpc.peterpan.com.core.protocol.RpcProtocol;
import rpc.peterpan.com.core.protocol.header.MsgHeader;
import rpc.peterpan.com.core.server.RpcServerWorker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.sleep;

/**
 * @author PeterPan
 * @date 2023/8/17
 * @description 自定义拒绝策略
 *
 * 发送异常状态2给客户端启动容错策略
 *
 */
@Slf4j
public class ExceptionStatusRejectedExecutionHandler implements RejectedExecutionHandler {
    @SneakyThrows
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r instanceof RpcServerWorker) {
            RpcServerWorker rpcServerWorker = (RpcServerWorker) r;
            Socket clientSocket = rpcServerWorker.getSocket();

            // 现在你可以使用 clientSocket 进行操作，比如发送一个带有异常状态的 RpcProtocol 对象
            ObjectOutputStream objectOutputStream = null;
            try {
                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                RpcProtocol rpcProtocol = new RpcProtocol();

                // 创建异常状态的消息头
                MsgHeader header = new MsgHeader();
                header.setStatus((byte) StatusConstants.EXCEPTION);
                rpcProtocol.setHeader(header);

                // 将带有异常状态的 RpcProtocol 对象发送到客户端
                objectOutputStream.writeObject(rpcProtocol);
                objectOutputStream.flush();
                log.warn("服务端向 {}:{} 发送了拒绝策略的信息", clientSocket.getInetAddress(), clientSocket.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sleep(5000);
                objectOutputStream.close(); // 这里的释放资源要间隔一段时间，因为这个输入不产生返回值我无法保证对方读取完整数据，延迟5s关闭
            }
        }
    }
}
