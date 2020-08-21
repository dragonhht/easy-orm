package com.gitee.carloshuang.handler;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * 加载生成的 Mapper实现类.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-21
 */
public class MapperClassLoader extends URLClassLoader {

    public MapperClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public MapperClassLoader(URL[] urls) {
        super(urls);
    }

    public MapperClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = super.loadClass(name);
        System.out.println("1: " + result.getName());
        return result;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = super.loadClass(name, resolve);
        System.out.println("2: " + result.getName());
        return result;
    }
}
