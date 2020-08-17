package com.gitee.carloshuang.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mapper标记接口动态代理类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class MapperProxyInvocationHandler implements InvocationHandler {

    private Object target;

    public MapperProxyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
