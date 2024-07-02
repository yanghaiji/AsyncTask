# AsyncTask

AsyncTask是Java并发工具包的扩展，它提供了一种简便的方式来管理和调度多线程执行的任务。
其主要功能是在多线程环境下，简化任务之间的依赖关系，从而使得开发者能够将更多的精力集中在业务逻辑的实现上，
而非任务调度的复杂性。通过使用 AsyncTask，开发者可以更加高效地处理并发任务，提高程序的性能和响应速度，
同时也降低了多线程编程的难度和风险。

# 参考手册

## Worker

通过Worker创建任务,同时可以指定失败之后的回调，让我们进行异常的处理

- 示例

```
    // 创建任务
    Worker<Integer, Integer> taskA = new Worker<>("A", () -> {
        sleep(1000);
        return 1;
    },
        @Override
        public Integer onFailure(Throwable t) {
            System.out.println("Task A failed with exception: " + t.getMessage());
            return -1;
        }
    });
```

##  TaskScheduler

核心的任务编排工具，包括了**任务的添加，运行，编排任务，等待其它任务完成后在执行当前任务**

```
 TaskScheduler scheduler = new TaskScheduler(4); 

 TaskScheduler schedulerAndShutdown = new SchedulerBuilder()
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
            .buildAndShutdown();
```

更多的使用方式请参考 https://github.com/yanghaiji/AsyncTask/tree/main/src/test/java/com/javayh/async/task