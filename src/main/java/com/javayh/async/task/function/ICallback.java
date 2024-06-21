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
     * 目前时在任务执行完成后，在finally 中执行，可以用于一些日志的记录
     *
     * @param request 回调函数
     */
    public void callback(T request) {
        Logger.info("callback is done , parameter : {}", request);
    }

    /**
     * 异常的处理
     *
     * @param t 异常
     * @return
     */
    public R onFailure(Throwable t) {
        Logger.error("DefaultCallback {}", t.getMessage());
        return null;
    }
}
