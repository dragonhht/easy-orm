package com.gitee.carloshuang.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Mapper标记接口动态代理类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class MapperProxyInvocationHandler<T> implements InvocationHandler {

    private T target;

    public MapperProxyInvocationHandler(T target) {
        this.target = target;
    }

    /**
     * 生成代理接口实例对象
     * @param <T>
     * @return 代理接口实例对象
     */
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
