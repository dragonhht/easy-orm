package com.gitee.carloshuang.exception;

/**
 * 无法创建jdbc url异常.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-18
 */
public class CanNotCreateJdbcUrlException  extends RuntimeException {

    public CanNotCreateJdbcUrlException() {
        super("无法创建jdbc url");
    }

    public CanNotCreateJdbcUrlException(String message) {
        super(message);
    }

    public CanNotCreateJdbcUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotCreateJdbcUrlException(Throwable cause) {
        super(cause);
    }

    public CanNotCreateJdbcUrlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
