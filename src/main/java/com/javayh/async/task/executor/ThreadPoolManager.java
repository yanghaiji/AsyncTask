package com.javayh.async.task.executor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池管理器
 *
 * @author HaiJiYang
 */
public class ThreadPoolManager {
    private static ThreadPoolManager instance;
    private final ConcurrentHashMap<String, ExecutorService> threadPools = new ConcurrentHashMap<>();
    private final AtomicInteger poolCount = new AtomicInteger(0);

    private ThreadPoolManager() {
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    public ExecutorService createThreadPool(int corePoolSize) {
        String poolName = "pool-" + poolCount.incrementAndGet();
        ExecutorService executor = new AsyncTaskExecutor(corePoolSize).getSingleton();
        threadPools.put(poolName, executor);
        return executor;
    }

    public void shutdownThreadPool(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownAll() {
        threadPools.values().forEach(this::shutdownThreadPool);
        threadPools.clear();
    }
}
