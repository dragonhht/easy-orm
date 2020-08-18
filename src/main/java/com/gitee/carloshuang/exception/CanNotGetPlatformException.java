package com.gitee.carloshuang.exception;

/**
 * 无法获取数据库平台类型.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-18
 */
public class CanNotGetPlatformException extends RuntimeException {

    public CanNotGetPlatformException() {
        super("法获取数据库平台类型");
    }

    public CanNotGetPlatformException(String message) {
        super(message);
    }

    public CanNotGetPlatformException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotGetPlatformException(Throwable cause) {
        super(cause);
    }

    public CanNotGetPlatformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
