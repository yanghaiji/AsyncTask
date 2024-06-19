package com.javayh.async.task.function;

import com.javayh.async.task.Logger;

/**
 * <p>
 * 任务处理器失败的回调
 * 已废弃,修改了ICallback的实现,子类根据需求,按需实现,而不是全部实现
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2024-06-17
 */
@Deprecated
public class DefaultCallback<T, R> extends ICallback<T, R> {

    /**
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
