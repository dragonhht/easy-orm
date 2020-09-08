package com.gitee.carloshuang.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

/**
 * 拦截器使用的参数.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-08
 */
@Data
@AllArgsConstructor
public class Invocation {
    /** 数据库连接. */
    private Connection connection;
    /** sql. */
    private String sql;
    /** 参数. */
    private Object[] params;
    /** 方法目标实例. */
    private Object target;
    /** 目标方法. */
    private Method method;

    /**
     * 执行目标方法
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return this.method.invoke(this.target, this.params);
    }
}
