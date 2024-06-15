package com.javayh.async.task;


/**
 * <p>
 * 统一的日志输出
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public class Logger {
    public static void log(String taskName, String message) {
        System.out.println("Task " + taskName + ": " + message);
    }
}
