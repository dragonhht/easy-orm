package com.gitee.carloshuang.interceptors;

import com.gitee.carloshuang.annotation.Interceptor;
import com.gitee.carloshuang.interceptor.HandlerInterceptor;
import com.gitee.carloshuang.model.Invocation;
import com.gitee.carloshuang.model.User;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-9-10
 */
@Interceptor(method = "com.gitee.carloshuang.mapper.TestMapper.getUserById", args = {User.class})
public class TestInterceptor implements HandlerInterceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
