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
public abstract class ICallback<T, R> {

    /**
     * 获取返回值
     *
     * @param r 传入的参数
     * @return
     */
    public final R result(R r) {
        return r;
    }

    /**
     * 失败后的回调函数,可以用于默认返回值的处理,或异常事件触发
     *
     * @param call 回调函数
     * @return
     */
    public R callback(T call) {
        return null;
    }

    /**
     * 异常的处理
     *
     * @param t 异常
     * @return
     */
    public R onFailure(Throwable t) {
        Logger.log("DefaultCallback", t.getMessage());
        return null;
    }
}
