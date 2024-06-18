package com.javayh.async.task;

import com.javayh.async.task.function.ICallback;
import com.javayh.async.task.scheduler.TaskScheduler;
import com.javayh.async.task.worker.Worker;

/**
 * <p>
 *
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class DummyDataCallback {
    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler(4);  // 设置线程池大小为4

        // 创建任务
        Worker<Integer, Integer> taskA = new Worker<>("A", () -> {
            sleep(1000);
            return 1;
        }, new ICallback<Integer, Integer>() {
            @Override
            public Integer callback(Integer result) {
                return result;
            }

            @Override
            public Integer onFailure(Throwable t) {
                System.out.println("Task A failed with exception: " + t.getMessage());
                return -1;
            }
        });

        Worker<Integer, Integer> taskE = new Worker<>("E", () -> {
            sleep(1000);
            return 2;
        }, new ICallback<Integer, Integer>() {
            @Override
            public Integer callback(Integer result) {
                return result;
            }

            @Override
            public Integer onFailure(Throwable t) {
                System.out.println("Task E failed with exception: " + t.getMessage());
                return -1;
            }
        });

        Worker<Integer, Integer> taskB = new Worker<>("B", () -> {
            sleep(1000);
            return 3 / 0;  // 模拟异常
        }, new ICallback<Integer, Integer>() {
            @Override
            public Integer callback(Integer result) {
                return result;
            }

            @Override
            public Integer onFailure(Throwable t) {
                return -1;
            }
        });

        Worker<Integer, Integer> taskC = new Worker<>("C", () -> {
            sleep(1000);
            return 4;
        }, new ICallback<Integer, Integer>() {
            @Override
            public Integer callback(Integer result) {
                return result;
            }

            @Override
            public Integer onFailure(Throwable t) {
                System.out.println("Task C failed with exception: " + t.getMessage());
                return -1;
            }
        });

        Worker<Integer, Integer> taskD = new Worker<>("D", () -> {
            sleep(1000);
            return 5;
        }, new ICallback<Integer, Integer>() {
            @Override
            public Integer callback(Integer result) {
                return result;
            }

            @Override
            public Integer onFailure(Throwable t) {
                System.out.println("Task D failed with exception: " + t.getMessage());
                return -1;
            }
        });

        // 添加任务
        scheduler.addTask(taskA, 2000);
        scheduler.addTask(taskE, 2000);
        scheduler.addTask(taskB, 2000);
        scheduler.addTask(taskC, 2000);
        scheduler.addTask(taskD, 2000);

        // 执行任务，注意顺序和依赖
        scheduler.runTask("A", 2000);
        scheduler.runTask("E", 2000);
        scheduler.runTaskAfter("B", 2000, "A");
        scheduler.runTaskAfter("C", 2000, "E");
        scheduler.runTaskAfter("D", 2000, "B", "C");

        scheduler.allOf("A", "B", "C", "D", "E");

        // 打印任务状态
        // 获取任务状态
        System.out.println("Task A status: " + scheduler.getTaskStatus("A") +" results : " + taskA.getResult());
        System.out.println("Task E status: " + scheduler.getTaskStatus("E") +" results : " + taskE.getResult());
        System.out.println("Task B status: " + scheduler.getTaskStatus("B") +" results : " + taskB.getResult());
        System.out.println("Task C status: " + scheduler.getTaskStatus("C") +" results : " + taskC.getResult());
        System.out.println("Task D status: " + scheduler.getTaskStatus("D") +" results : " + taskD.getResult());

        // 关闭任务调度器
        scheduler.shutdown();
        System.out.println("All tasks are completed");
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
