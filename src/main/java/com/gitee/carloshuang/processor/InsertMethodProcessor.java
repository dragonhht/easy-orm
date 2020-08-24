package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.Insert;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 新增方法处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-24
 */
class InsertMethodProcessor {

    /**
     * 解析 @Insert 注解标记的方法
     * @param method
     * @return
     */
    public static MethodSpec parserInsertMethod(Method method) {
        // 解析SQL
        Insert anno = method.getDeclaredAnnotation(Insert.class);
        String sql = anno.value();

        // 创建实现类方法源代码
        Parameter[] params = method.getParameters();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(method.getReturnType());
        for (Parameter param : params) {
            methodBuilder.addParameter(param.getType(), param.getName());
        }

        return methodBuilder.build();
    }

}
