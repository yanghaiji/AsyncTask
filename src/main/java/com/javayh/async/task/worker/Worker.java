package com.javayh.async.task.worker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.javayh.async.task.Logger;


/**
 * <p>
 * 统一的日志输出
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class Worker<T> implements Task<T> {
    /**
     * 任务名称
     */
    private final String name;

    /**
     * 执行的线程
     */
    private final Supplier<T> task;

    /**
     * 返回值
     */
    private T result;

    public Worker(String name, Supplier<T> task) {
        this.name = name;
        this.task = task;
    }

    /**
     * 需要运行的任务现成
     *
     * @param defaultValue  默认的返回值
     * @param executor      自定义的线程池
     * @param timeoutMillis 超时时间
     * @return
     */
    @Override
    public CompletableFuture<T> runAsync(long timeoutMillis, T defaultValue, ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Logger.log(name, "is running");
                T result = task.get();
                Logger.log(name, "is completed with result: " + result);
                this.result = result;
                return result;
            } catch (Exception e) {
                Logger.log(name, "failed with exception: " + e.getMessage());
                return defaultValue;
            }
        }, executor)
            .exceptionally(ex -> {
                Logger.log(name, "failed with timeout or exception: " + ex.getMessage());
                this.result = defaultValue;
                return defaultValue;
            });
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getResult() {
        return result;
    }
}
