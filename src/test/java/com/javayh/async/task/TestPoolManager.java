package com.javayh.async.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import com.javayh.async.task.executor.AsyncTaskExecutor;

/**
 * <p>
 *
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
public class TestPoolManager {
    public static void main(String[] args) throws InterruptedException {
        AsyncTaskExecutor asyncTaskExecutor = new AsyncTaskExecutor(3);
        ThreadPoolExecutor singleton = asyncTaskExecutor.getSingleton();
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            singleton.execute(()->{
                new DummyData().test();
                latch.countDown();
            });
        }
        latch.await();

        System.out.println("多任务全部完成");
        singleton.shutdown();
    }
}
