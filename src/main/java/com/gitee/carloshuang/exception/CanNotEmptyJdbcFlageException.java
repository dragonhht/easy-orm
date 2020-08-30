package com.gitee.carloshuang.exception;

/**
 * 数据源标识为空异常.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-30
 */
public class CanNotEmptyJdbcFlageException  extends RuntimeException {

    public CanNotEmptyJdbcFlageException() {
        super("数据源标识不能为空");
    }

    public CanNotEmptyJdbcFlageException(String message) {
        super(message);
    }

    public CanNotEmptyJdbcFlageException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotEmptyJdbcFlageException(Throwable cause) {
        super(cause);
    }

    public CanNotEmptyJdbcFlageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
