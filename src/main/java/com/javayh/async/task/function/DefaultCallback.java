package com.javayh.async.task.function;

import com.javayh.async.task.Logger;

/**
 * <p>
 * 任务处理器失败的回调
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
public class DefaultCallback<T, R> implements ICallback<T, R> {

    /**
     * 失败后的回调函数,可以用于默认返回值的处理,或异常事件触发
     *
     * @param call 回调函数
     * @return
     */
    @Override
    public R callback(T call) {
        return (R) call;
    }

    /**
     * 异常的处理
     *
     * @param t 异常
     * @return
     */
    @Override
    public R onFailure(Throwable t) {
        Logger.log("DefaultCallback", t.getMessage());
        return null;
    }
}
