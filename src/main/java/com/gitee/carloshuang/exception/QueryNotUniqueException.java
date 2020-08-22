package com.gitee.carloshuang.exception;

/**
 * 查询的结果不唯一.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-22
 */
public class QueryNotUniqueException extends RuntimeException {

    public QueryNotUniqueException() {
        super("The result of the query is not unique");
    }

    public QueryNotUniqueException(String message) {
        super(message);
    }

    public QueryNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryNotUniqueException(Throwable cause) {
        super(cause);
    }

    public QueryNotUniqueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
