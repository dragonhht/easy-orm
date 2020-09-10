package com.gitee.carloshuang.handler;

import com.gitee.carloshuang.annotation.MethodId;
import com.gitee.carloshuang.model.Invocation;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.storage.SqlContainer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

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
        Invocation invocation = createInvocation(proxy, method, args);
        if (invocation == null) return method.invoke(target, args);
        return com.gitee.carloshuang.handler.InvocationHandler.handler(invocation);
    }

    /**
     * 创建拦截器使用的参数
     * @param proxy 目标实例
     * @param method 目标方法
     * @param args 参数
     * @return
     */
    private Invocation createInvocation(Object proxy, Method method, Object[] args) {
        Connection connection = ConnectionHolder.getInstance().getConnection();
        MethodId methodId = method.getDeclaredAnnotation(MethodId.class);
        if (methodId == null) return null;
        String id = methodId.id();
        String sql = SqlContainer.getInstance().get(id);
        return new Invocation(connection, sql, args, target, method);
    }
}
