package com.javayh.async.task.executor;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 线程池
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
public class AsyncTaskExecutor {

    private final ThreadPoolExecutor singleton;

    public AsyncTaskExecutor(int core) {
        singleton = new ThreadPoolExecutor(core, core, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    }

    public ThreadPoolExecutor getSingleton() {
        return singleton;
    }
}
