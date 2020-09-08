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

}
