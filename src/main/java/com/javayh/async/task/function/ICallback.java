package com.javayh.async.task.function;

/**
 * <p>
 * 任务处理器失败的回调
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
public interface ICallback<T, R> {

    /**
     * 失败后的回调函数,可以用于默认返回值的处理,或异常事件触发
     *
     * @param call 回调函数
     * @return
     */
    R callback(T call);

    /**
     * 异常的处理
     *
     * @param t 异常
     * @return
     */
    R onFailure(Throwable t);
}
