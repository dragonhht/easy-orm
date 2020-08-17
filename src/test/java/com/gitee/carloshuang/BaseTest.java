package com.gitee.carloshuang;

import com.gitee.carloshuang.annotation.Mapper;
import org.junit.Test;
import org.reflections.Reflections;

import javax.annotation.processing.RoundEnvironment;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class BaseTest {

    @Test
    public void testGetAnno() {
        Reflections f = new Reflections("");
        Set<Class<?>> set = f.getTypesAnnotatedWith(Mapper.class);
        for (Class<?> aClass : set) {
            System.out.println(aClass.getName());
        }
    }

}
