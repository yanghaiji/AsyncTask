package com.javayh.async.task.scheduler;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.javayh.async.task.Logger;
import com.javayh.async.task.TaskStatus;
import com.javayh.async.task.worker.Task;


/**
 * <p>
 * 任务编排
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class TaskScheduler {

    private final Map<String, Task<?>> tasks = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<?>> futures = new ConcurrentHashMap<>();
    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final ExecutorService executor;


    public TaskScheduler(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public <T> void addTask(Task<T> task, long timeoutMillis, T defaultValue) {
        tasks.put(task.getName(), task);
        taskStatuses.put(task.getName(), TaskStatus.PENDING);
        CompletableFuture<T> future = task.runAsync(timeoutMillis, defaultValue, executor)
            .thenApply(result -> {
                taskStatuses.put(task.getName(), TaskStatus.COMPLETED);
                return result;
            })
            .exceptionally(ex -> {
                taskStatuses.put(task.getName(), TaskStatus.FAILED);
                return defaultValue;
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
     * @param defaultValue  默认的返回值
     * @param dependencies  依赖的任务
     * @param <T>
     */
    public <T> void runTaskAfter(String taskName, long timeoutMillis, T defaultValue, String... dependencies) {
        CompletableFuture<Void> dependencyFuture = CompletableFuture.allOf(Arrays.stream(dependencies)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        // fix  thenComposeAsync 确保依赖任务完成后执行下一个任务。
        CompletableFuture<Object> future = dependencyFuture.thenComposeAsync(v -> {
            taskStatuses.put(taskName, TaskStatus.RUNNING);
            return getTask(taskName).runAsync(timeoutMillis, defaultValue, executor)
                .thenApply(result -> {
                    taskStatuses.put(taskName, TaskStatus.COMPLETED);
                    return result;
                })
                .exceptionally(ex -> {
                    taskStatuses.put(taskName, TaskStatus.FAILED);
                    return defaultValue;
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
     * @param defaultValue  默认的返回值
     * @param <T>
     */
    public <T> void runTask(String taskName, long timeoutMillis, T defaultValue) {
        taskStatuses.put(taskName, TaskStatus.RUNNING);
        futures.put(taskName, getTask(taskName).runAsync(timeoutMillis, defaultValue, executor));
    }

    /**
     * 在所有依赖的任务完成后执行一个给定的任务，并收集依赖任务的结果作为输入参数传递给这个任务。
     * 在任务B和C完成后，使用返回值启动任务D
     *
     * @param taskName      任务的名字
     * @param timeoutMillis 超时时间，暂时没有完善，
     *                      可以升级jdk版本(jdk8不支持)，使用.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)实现
     * @param defaultValue  默认的返回值
     * @param taskFunction  当前执行的任务所依赖的返回值
     * @param dependencies  依赖的任务
     * @param <T>
     */
    public <T> void runTaskAfterWithResult(String taskName, Function<Map<String, Object>, T> taskFunction,
                                           long timeoutMillis, T defaultValue, String... dependencies) {
        CompletableFuture<Void> dependencyFuture = CompletableFuture.allOf(Arrays.stream(dependencies)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        // fix  thenComposeAsync 确保依赖任务完成后执行下一个任务。
        CompletableFuture<T> future = dependencyFuture.thenComposeAsync(v -> {
            Map<String, Object> results = new ConcurrentHashMap<>();
            for (String dependency : dependencies) {
                results.put(dependency, futures.get(dependency).join());
            }
            return CompletableFuture.supplyAsync(() -> {
                try {
                    taskStatuses.put(taskName, TaskStatus.RUNNING);
                    T result = taskFunction.apply(results);
                    taskStatuses.put(taskName, TaskStatus.COMPLETED);
                    return result;
                } catch (Exception e) {
                    taskStatuses.put(taskName, TaskStatus.FAILED);
                    Logger.log(taskName, "failed with exception: " + e.getMessage());
                    return defaultValue;
                }
            }, executor);
        }, executor)
            .exceptionally(ex -> {
                Logger.log(taskName, "failed with exception: " + ex.getMessage());
                return defaultValue;
            });

        futures.put(taskName, future);
    }

    /**
     * 等待所有任务完成
     *
     * @param taskNames 任务的名称
     */
    public void allOf(String... taskNames) {
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(Arrays.stream(taskNames)
            .map(futures::get)
            .toArray(CompletableFuture[]::new));
        try {
            allTasks.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TaskStatus getTaskStatus(String taskName) {
        return taskStatuses.get(taskName);
    }

    @SuppressWarnings("unchecked")
    private <T> Task<T> getTask(String name) {
        return (Task<T>) tasks.get(name);
    }

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
        }
    }
}
