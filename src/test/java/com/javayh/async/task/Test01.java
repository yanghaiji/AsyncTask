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
 * @since 2024-06-14
 */
public class Test01 {
    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler(4);  // 设置线程池大小为4

        // 创建任务
        Worker<Integer,Integer> taskA = new Worker<>("A", () -> {
            sleep(1000);
            return 1;
        });

        Worker<Integer,Integer>taskE = new Worker<>("E", () -> {
            sleep(1000);
            return 2;
        });

        Worker<Integer,Integer> taskB = new Worker<>("B", () -> {
            sleep(1000);
            return 3;
        });

        Worker<Integer,Integer>taskC = new Worker<>("C", () -> {
            sleep(1000);
            return 4;
        });

        Worker<Integer,Integer> taskD = new Worker<>("D", () -> {
            sleep(1000);
            return 5;
        });

        // 添加任务到调度器，设置超时和默认值
        scheduler.addTask(taskA, 2000);
        scheduler.addTask(taskE, 2000);
        scheduler.addTask(taskB, 2000);
        scheduler.addTask(taskC, 2000);
        scheduler.addTask(taskD, 2000);

        // 定义任务的依赖关系
        scheduler.runTask("A", 2000); // 启动任务A
        scheduler.runTask("E", 2000); // 启动任务E
        scheduler.runTaskAfter("B", 2000, "A"); // 在任务A完成后启动任务B
        scheduler.runTaskAfter("C", 2000, "A"); // 在任务A完成后启动任务C

        // 在任务B和C完成后，使用返回值启动任务D
        scheduler.runTaskAfterWithResult("D", results -> {
            int resultB = (int) results.get("B");
            int resultC = (int) results.get("C");
            System.out.println("Results from B and C: " + resultB + ", " + resultC);
            return resultB + resultC;
        }, 2000, "B", "C");

        // 等待所有任务完成
        scheduler.allOf("A", "E", "B", "C", "D");

        // 获取任务状态
        System.out.println("Task A status: " + scheduler.getTaskStatus("A") +" results : " + taskA.getResult());
        System.out.println("Task E status: " + scheduler.getTaskStatus("E") +" results : " + taskE.getResult());
        System.out.println("Task B status: " + scheduler.getTaskStatus("B") +" results : " + taskB.getResult());
        System.out.println("Task C status: " + scheduler.getTaskStatus("C") +" results : " + taskC.getResult());
        System.out.println("Task D status: " + scheduler.getTaskStatus("D") +" results : " + taskD.getResult());

        System.out.println("All tasks are completed");

        // 关闭调度器，释放资源
        scheduler.shutdown();
    }

    // 辅助方法，用于模拟任务执行
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
