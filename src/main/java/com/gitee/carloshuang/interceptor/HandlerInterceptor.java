package com.gitee.carloshuang.interceptor;

import com.gitee.carloshuang.model.Invocation;

/**
 * 逻辑处理拦截器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-31
 */
public interface HandlerInterceptor {

    /**
     * 拦截逻辑
     * @param invocation
     * @return
     * @throws Throwable
     */
    Object intercept(Invocation invocation) throws Throwable;

}
