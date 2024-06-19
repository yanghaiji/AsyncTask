package com.javayh.async.task;

import com.javayh.async.task.scheduler.TaskScheduler;
import com.javayh.async.task.worker.Worker;

/**
 * <p>
 *
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-18
 */
public class TestBuilder {
    public static void main(String[] args) {

        // 创建任务
        Worker<Integer, Integer> taskA = new Worker<>("A", () -> {
            sleep(1000);
            return 1;
        });

        Worker<Integer, Integer> taskB = new Worker<>("B", () -> {
            sleep(1000);
            return 3;
        });

        Worker<Integer, Integer> taskC = new Worker<>("C", () -> {
            sleep(1000);
            return 4;
        });

        Worker<Integer, Integer> taskD = new Worker<>("D", () -> {
            sleep(1000);
            return 5;
        });

        TaskScheduler scheduler = new SchedulerBuilder()
            .setThreadPoolSize(20)
            .addTask(taskA, 1000)
            .addTask(taskB, 2000)
            .addTask(taskC, 2000)
            .addTask(taskD, 2000)
            .runTask("A", 3000)
            .runTask("B", 3000)
            .runTaskAfter("C", 3000, "A", "B")
            .runTaskAfterWithResult("D", resultMap -> {
                // 使用resultMap处理结果
                return 5;
            }, 4000, "C")
            .build();
        // 等待所有任务完成
        scheduler.allOf("A", "B", "C", "D");
        scheduler.shutdown();

    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
