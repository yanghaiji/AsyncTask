package com.javayh.async.task;


/**
 * <p>
 * 当前任务状态
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-14
 */
public enum TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMEOUT
}