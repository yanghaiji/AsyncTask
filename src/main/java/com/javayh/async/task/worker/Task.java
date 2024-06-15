package com.javayh.async.task.worker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 * 统一的任务接口
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public interface Task<T> {

    /**
     * 需要运行的任务现成
     *
     * @param defaultValue 默认的返回值
     * @param executor     自定义的线程池
     * @return
     */
    CompletableFuture<T> runAsync(T defaultValue, ExecutorService executor);

    /**
     * 当前任务的名字
     *
     * @return
     */
    String getName();

}