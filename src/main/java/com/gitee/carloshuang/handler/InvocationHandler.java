package com.gitee.carloshuang.handler;

import com.gitee.carloshuang.model.Invocation;
import lombok.SneakyThrows;

/**
 * 执行处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-10
 */
public class InvocationHandler {

    /**
     * 处理语句，先执行前置拦截
     * @param invocation
     * @return
     */
    @SneakyThrows
    public static Object handler(Invocation invocation) {
        return invocation.proceed();
    }

}
