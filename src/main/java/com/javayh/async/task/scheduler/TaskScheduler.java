package com.javayh.async.task.scheduler;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.javayh.async.task.Logger;
import com.javayh.async.task.TaskStatus;
import com.javayh.async.task.exception.TaskSchedulerException;
import com.javayh.async.task.executor.ThreadPoolManager;
import com.javayh.async.task.function.SchedulerExecutor;
import com.javayh.async.task.function.Task;


/**
 * <p>
 * 任务编排
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class TaskScheduler implements SchedulerExecutor {

    private final Map<String, Task<?, ?>> tasks = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<?>> futures = new ConcurrentHashMap<>();
    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final ThreadPoolManager poolManager;

    public TaskScheduler(int threadPoolSize) {
        poolManager = ThreadPoolManager.getInstance();
        this.executor = poolManager.createThreadPool(threadPoolSize);
    }

    @Override
    public <T, R> void addTask(Task<T, R> task, long timeoutMillis) {
        tasks.put(task.getName(), task);
        taskStatuses.putIfAbsent(task.getName(), TaskStatus.PENDING);
        CompletableFuture<R> future = task.runAsync(timeoutMillis, executor)
            .thenApply(result -> {
                taskStatuses.put(task.getName(), TaskStatus.COMPLETED);
                return result;
            })
            .exceptionally(ex -> {
                taskStatuses.put(task.getName(), TaskStatus.FAILED);
                return null;
            });
        futures.put(task.getName(), future);
    }

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
    @Override
    public <T, R> void runTaskAfter(String taskName, long timeoutMillis, String... dependencies) {
        CompletableFuture<Void> dependencyFuture = CompletableFuture.allOf(Arrays.stream(dependencies)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        // fix  thenComposeAsync 确保依赖任务完成后执行下一个任务。
        CompletableFuture<Object> future = dependencyFuture.thenComposeAsync(v -> {
            taskStatuses.put(taskName, TaskStatus.RUNNING);
            return getTask(taskName).runAsync(timeoutMillis, executor)
                .thenApply(result -> {
                    taskStatuses.put(taskName, TaskStatus.COMPLETED);
                    return result;
                })
                .exceptionally(ex -> {
                    taskStatuses.put(taskName, TaskStatus.FAILED);
                    throw new TaskSchedulerException(ex.toString());
                });
        }, executor);

        futures.put(taskName, future);
    }

    /**
     * 执行任务
     *
     * @param taskName      任务的名字
     * @param timeoutMillis 超时时间，暂时没有完善，
     *                      可以升级jdk版本(jdk8不支持)，使用.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)实现
     * @param <T>
     */
    @Override
    public <T, R> void runTask(String taskName, long timeoutMillis) {
        if (taskStatuses.get(taskName) != TaskStatus.PENDING) {
            Logger.log(taskName, "is already running or completed.");
            return;
        }
        taskStatuses.put(taskName, TaskStatus.RUNNING);
        futures.put(taskName, getTask(taskName).runAsync(timeoutMillis, executor));
    }

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
    @Override
    public <T, R> void runTaskAfterWithResult(String taskName, Function<Map<String, Object>, R> taskFunction,
                                              long timeoutMillis, String... dependencies) {
        CompletableFuture<Void> dependencyFuture = CompletableFuture.allOf(Arrays.stream(dependencies)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        // fix  thenComposeAsync 确保依赖任务完成后执行下一个任务。
        CompletableFuture<R> future = dependencyFuture.thenComposeAsync(v -> {
            Map<String, Object> results = new ConcurrentHashMap<>();
            for (String dependency : dependencies) {
                results.put(dependency, futures.get(dependency).join());
            }
            return CompletableFuture.supplyAsync(() -> {
                try {
                    taskStatuses.put(taskName, TaskStatus.RUNNING);
                    R result = taskFunction.apply(results);
                    taskStatuses.put(taskName, TaskStatus.COMPLETED);
                    return result;
                } catch (Exception e) {
                    taskStatuses.put(taskName, TaskStatus.FAILED);
                    Logger.log(taskName, "failed with exception: " + e.getMessage());
                    throw new TaskSchedulerException(e);
                }
            }, executor);
        }, executor)
            .exceptionally(ex -> {
                Logger.log(taskName, "failed with exception: " + ex.getMessage());
                throw new TaskSchedulerException("failed with exception: " + ex);
            });

        futures.put(taskName, future);
    }

    /**
     * 等待所有任务完成
     *
     * @param taskNames 任务的名称
     */
    @Override
    public void allOf(String... taskNames) {
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(Arrays.stream(taskNames)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        try {
            allTasks.join();
        } catch (Exception e) {
            throw new TaskSchedulerException(e);
        }
    }

    @Override
    public TaskStatus getTaskStatus(String taskName) {
        return taskStatuses.get(taskName);
    }

    @SuppressWarnings("unchecked")
    private <T, R> Task<T, R> getTask(String name) {
        return (Task<T, R>) tasks.get(name);
    }

    @Override
    public void shutdown() {
        // Ensure all tasks are completed before shutting down
        allOf(tasks.keySet().toArray(new String[0]));
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
        } finally {
            poolManager.shutdownThreadPool(executor);
            Logger.log("Thread Pool", "关闭成功");
        }
    }
}
