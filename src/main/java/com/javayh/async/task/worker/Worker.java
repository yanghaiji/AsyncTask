package com.javayh.async.task.worker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import com.javayh.async.task.Logger;
import com.javayh.async.task.exception.WorkException;
import com.javayh.async.task.function.DefaultCallback;
import com.javayh.async.task.function.ICallback;
import com.javayh.async.task.function.Task;


/**
 * <p>
 * 统一的日志输出
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class Worker<T, R> implements Task<T, R> {
    /**
     * 任务名称
     */
    private final String name;

    /**
     * 执行的线程
     */
    private final Supplier<R> task;

    /**
     * 返回值
     */
    private R result;

    /**
     * 请求的回调函数
     */
    private final ICallback<T, R> callback;

    public Worker(String name, Supplier<R> task) {
        this.name = name;
        this.task = task;
        this.callback = new DefaultCallback<>();
    }

    public Worker(String name, Supplier<R> task, ICallback<T, R> callback) {
        this.name = name;
        this.task = task;
        this.callback = callback;
    }

    /**
     * 需要运行的任务现成
     *
     * @param executor      自定义的线程池
     * @param timeoutMillis 超时时间
     * @return
     */
    @Override
    public CompletableFuture<R> runAsync(long timeoutMillis, ExecutorService executor) throws WorkException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                R result = task.get();
                Logger.info("{} is completed with result: {}", name, result);
                R res = this.callback.result(result);
                this.result = res;
                return res;
            } catch (Exception e) {
                Logger.error(name, "failed with exception: {}", e.getMessage());
                R res = callback.onFailure(e);
                this.result = res;
                return res;
            }
        }, executor)
            .exceptionally(ex -> {
                Logger.error(" {} failed with timeout or exception: {}", name, ex.getMessage());
                R res = callback.onFailure(ex);
                this.result = res;
                return res;
            });
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public R getResult() {
        return result;
    }

}
