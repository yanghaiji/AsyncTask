package com.javayh.async.task;


import org.apache.logging.log4j.LogManager;

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

    private Logger() {

    }

    private static Class<?> getCallingClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] 是getStackTrace()本身
        // stackTrace[1] 是getCallingClass()方法
        // stackTrace[2] 是调用getCallingClass()的工具类方法
        // stackTrace[3] 是调用工具类方法的实际调用者
        try {
            return Class.forName(stackTrace[3].getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    public static void debug(String message, Object... args) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(getCallingClass());
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }

    public static void info(String message, Object... args) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(getCallingClass());
        if (logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }

    public static void warn(String message, Object... args) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(getCallingClass());
        if (logger.isWarnEnabled()) {
            logger.warn(message, args);
        }
    }

    public static void error(String message, Throwable t, Object... args) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(getCallingClass());
        if (logger.isErrorEnabled()) {
            logger.error(String.format(message, args), t);
        }
    }

    public static void error(String message, Object... args) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(getCallingClass());
        if (logger.isErrorEnabled()) {
            logger.error(message, args);
        }
    }

}
