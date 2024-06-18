package com.javayh.async.task.function;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.javayh.async.task.exception.WorkException;

/**
 * <p>
 * 统一的任务接口
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public interface Task<T,R> {

    /**
     * 需要运行的任务现成
     *
     * @param executor      自定义的线程池
     * @param timeoutMillis 超时时间
     * @return
     * @throws WorkException
     */
    CompletableFuture<R> runAsync(long timeoutMillis, ExecutorService executor) throws WorkException;

    /**
     * 当前任务的名字
     *
     * @return
     */
    String getName();

    /**
     * 获取返回值
     *
     * @return
     */
    R getResult();

}