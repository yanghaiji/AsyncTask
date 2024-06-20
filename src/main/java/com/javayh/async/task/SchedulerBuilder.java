package com.javayh.async.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import com.javayh.async.task.function.Task;
import com.javayh.async.task.scheduler.TaskScheduler;

/**
 * @author HaiJiYang
 */
public class SchedulerBuilder {
    private int threadPoolSize;
    private final List<TaskConfig> taskConfigs = new ArrayList<>();

    public SchedulerBuilder setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        return this;
    }

    public <T, R> SchedulerBuilder addTask(Task<T, R> task, long timeoutMillis) {
        taskConfigs.add(new TaskConfig(task, timeoutMillis));
        return this;
    }

    public SchedulerBuilder runTaskAfter(String taskName, long timeoutMillis, String... dependencies) {
        taskConfigs.add(new TaskConfig(taskName, timeoutMillis, dependencies, false));
        return this;
    }

    public <T, R> SchedulerBuilder runTaskAfterWithResult(String taskName, Function<Map<String, Object>, R> taskFunction,
                                                          long timeoutMillis, String... dependencies) {
        taskConfigs.add(new TaskConfig(taskName, taskFunction, timeoutMillis, dependencies));
        return this;
    }

    public SchedulerBuilder runTask(String taskName, long timeoutMillis) {
        taskConfigs.add(new TaskConfig(taskName, timeoutMillis));
        return this;
    }

    public TaskScheduler build() {
        TaskScheduler scheduler = new TaskScheduler(threadPoolSize);

        for (TaskConfig config : taskConfigs) {
            if (config.task != null) {
                scheduler.addTask(config.task, config.timeoutMillis);
            } else if (config.taskFunction != null) {
                scheduler.runTaskAfterWithResult(config.taskName, config.taskFunction, config.timeoutMillis, config.dependencies);
            } else if (config.dependencies != null) {
                scheduler.runTaskAfter(config.taskName, config.timeoutMillis, config.dependencies);
            } else {
                scheduler.runTask(config.taskName, config.timeoutMillis);
            }
        }
        return scheduler;
    }

    /**
     * 创建并在关闭线程的任务链路
     * @return
     */
    public TaskScheduler buildAndShutdown() {
        TaskScheduler scheduler = new TaskScheduler(threadPoolSize);
        long count = taskConfigs.stream().filter((config) -> config.task != null).count();
        CountDownLatch latch = new CountDownLatch((int) count);
        try {
            for (TaskConfig config : taskConfigs) {
                if (config.task != null) {
                    scheduler.addTask(config.task, config.timeoutMillis);
                } else if (config.taskFunction != null) {
                    scheduler.runTaskAfterWithResult(config.taskName, config.taskFunction, config.timeoutMillis,
                        config.dependencies);
                    latch.countDown();
                } else if (config.dependencies != null) {
                    scheduler.runTaskAfter(config.taskName, config.timeoutMillis, config.dependencies);
                    latch.countDown();
                } else {
                    scheduler.runTask(config.taskName, config.timeoutMillis);
                    latch.countDown();
                }
            }
            latch.await(); // 等待所有任务完成
        } catch (Exception e) {
            Logger.error("TaskSchedulerBuilder {}", e);
        } finally {
            scheduler.shutdown();
            Logger.info("任务执行完成，自定关闭线程");
        }
        return scheduler;
    }

    private static class TaskConfig<T, R> {
        Task<T, R> task;
        long timeoutMillis;
        String taskName;
        String[] dependencies;
        Function<Map<String, Object>, R> taskFunction;

        TaskConfig(Task<T, R> task, long timeoutMillis) {
            this.task = task;
            this.timeoutMillis = timeoutMillis;
        }

        TaskConfig(String taskName, long timeoutMillis) {
            this.taskName = taskName;
            this.timeoutMillis = timeoutMillis;
        }

        TaskConfig(String taskName, long timeoutMillis, String[] dependencies, boolean withResult) {
            this.taskName = taskName;
            this.timeoutMillis = timeoutMillis;
            this.dependencies = dependencies;
        }

        TaskConfig(String taskName, Function<Map<String, Object>, R> taskFunction, long timeoutMillis, String[] dependencies) {
            this.taskName = taskName;
            this.taskFunction = taskFunction;
            this.timeoutMillis = timeoutMillis;
            this.dependencies = dependencies;
        }
    }
}