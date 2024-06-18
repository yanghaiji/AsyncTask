package com.javayh.async.task.function;

import java.util.Map;
import java.util.function.Function;

import com.javayh.async.task.TaskStatus;

/**
 * <p>
 * 任务编排
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
public interface SchedulerExecutor {

    /**
     * 将任务添加到调度中
     *
     * @param task          任务
     * @param timeoutMillis 超时时间
     * @param <T>
     */
    <T, R> void addTask(Task<T, R> task, long timeoutMillis);


    /**
     * 存在依赖关系的任务执行器
     * 在任务B和C完成后，使用返回值启动任务D
     *
     * @param taskName      任务的名字
     * @param timeoutMillis 超时时间，暂时没有完善，
     *                      可以升级jdk版本(jdk8不支持)，使用.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)实现
     * @param dependencies  依赖的任务
     * @param <T>
     */
    <T, R> void runTaskAfter(String taskName, long timeoutMillis, String... dependencies);


    /**
     * 执行任务
     *
     * @param taskName      任务的名字
     * @param timeoutMillis 超时时间，暂时没有完善，
     *                      可以升级jdk版本(jdk8不支持)，使用.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)实现
     * @param <T>
     */
    <T, R> void runTask(String taskName, long timeoutMillis);

    /**
     * 在所有依赖的任务完成后执行一个给定的任务，并收集依赖任务的结果作为输入参数传递给这个任务。
     * 在任务B和C完成后，使用返回值启动任务D
     *
     * @param taskName      任务的名字
     * @param timeoutMillis 超时时间，暂时没有完善，
     *                      可以升级jdk版本(jdk8不支持)，使用.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)实现
     * @param taskFunction  当前执行的任务所依赖的返回值
     * @param dependencies  依赖的任务
     * @param <T>
     */
    <T, R> void runTaskAfterWithResult(String taskName, Function<Map<String, Object>, R> taskFunction,
                                       long timeoutMillis, String... dependencies);

    /**
     * 等待所有任务完成
     *
     * @param taskNames 任务的名称
     */
    void allOf(String... taskNames);


    /**
     * 获取线程的当前状态
     *
     * @param taskName 线程的名字
     * @return {@link TaskStatus}
     */
    TaskStatus getTaskStatus(String taskName);

    /**
     * 关闭线程
     */
    void shutdown();
}
