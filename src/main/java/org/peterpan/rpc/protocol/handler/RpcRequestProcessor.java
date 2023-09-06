package org.peterpan.rpc.protocol.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author PeterPan
 * @date 2023/7/18
 * @description 业务线程池
 *
 * 提交 RPC 请求的处理任务到线程池进行异步执行
 * 线程池的创建是通过双重检查锁定的方式实现的，确保线程池只被创建一次
 * 实现了自定义的拒绝策略，和线程池性能阈值报警。这种方式可以减少主线程对线程池状态的频繁检测，从而提高性能
 */
//public class RpcRequestProcessor {
//
//    private static ThreadPoolExecutor threadPoolExecutor;
//
//    public static void submitRequest(Runnable task) {
//        if (threadPoolExecutor == null) {
//            synchronized (RpcRequestProcessor.class) {
//                if (threadPoolExecutor == null) {
//                    //核心线程数为 10，最大线程数为 10，线程空闲时间为 60 秒，使用 ArrayBlockingQueue 作为阻塞队列，队列大小为 10000
//                    threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
//                }
//            }
//        }
//        //submit() 方法接受一个 Runnable 任务对象，并将其提交给线程池进行执行。它返回一个 Future 对象，用于表示任务的异步执行结果
//        threadPoolExecutor.submit(task);
//    }
//}
@Slf4j
public class RpcRequestProcessor {

    private static ThreadPoolExecutor threadPoolExecutor;
    private static volatile boolean isFull = false;

    public static void submitRequest(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (RpcRequestProcessor.class) {
                if (threadPoolExecutor == null) {
                    RejectedExecutionHandler rejectedExecutionHandler = new CustomRejectionHandler();
                    //核心线程数为 2，最大线程数为 10，线程空闲时间为 60 秒，使用 ArrayBlockingQueue 作为阻塞队列，队列大小为 10000
                    threadPoolExecutor = new ThreadPoolExecutor(2, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000), rejectedExecutionHandler);
                    //开启监控线程
                    startMonitoringThread();
                }
            }
        }
        //submit() 方法接受一个 Runnable 任务对象，并将其提交给线程池进行执行。它返回一个 Future 对象，用于表示任务的异步执行结果
        threadPoolExecutor.submit(task);
    }

    public static void startMonitoringThread() {
        Thread monitoringThread = new Thread(() -> {
            while (!isFull) { // 循环直到isFull为true
                int activeCount = threadPoolExecutor.getActiveCount();
                int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();

                if ((double) activeCount / maxPoolSize >= 0.7) {
                    log.warn("Warning! Thread pool usage is at 70% of its capacity.");
                }

                try {
                    Thread.sleep(5000); // 5秒检测一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        monitoringThread.start();
    }

    private static class CustomRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("Warning! Task rejected: " + r.toString());
            isFull = true;
        }
    }
}
