package com.gitee.carloshuang.utils;

import com.gitee.carloshuang.annotation.MethodId;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;

/**
 * 方法Id注解标记生成工具.
 *
 * @author: Carlos Huang
 * @Date: 2020-9-08
 */
public class MethodIdAnnotationUtils {

    /**
     * 获取 @MethodId 注解
     * @param id
     * @return
     */
    public static AnnotationSpec getAnnotationSpec(String id) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("$S", id);
        return AnnotationSpec.builder(MethodId.class)
                .addMember("id", codeBlockBuilder.build())
                .build();
    }

    /**
     * 拦截器 methodId
     * @param method 全限定方法名
     * @param args 参数类型
     * @return methodId
     */
    public static String interceptorMethod(String method, Class<?>[] args) {
        StringBuilder sb = new StringBuilder(method)
                .append("_");
        for (Class<?> arg : args) {
            sb.append(arg.getName()).append("_");
        }
        return sb.toString();
    }

}
