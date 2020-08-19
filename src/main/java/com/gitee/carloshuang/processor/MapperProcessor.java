package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.*;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.Set;

/**
 * Mapper注解处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class MapperProcessor {

    private static final MapperProcessor PROCESSOR = new MapperProcessor();
    /** 包含该注解的类. */
    private Set<Class<?>> classSet;

    private MapperProcessor() {
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        // 获取有包含Mapper注解的类
        Reflections f = new Reflections("");
        classSet = f.getTypesAnnotatedWith(Mapper.class);
    }

    /**
     * 处理带Mapper注解的接口
     */
    private void process() {
        if (CollectionUtils.isEmpty(classSet)) return;
        for (Class<?> aClass : classSet) {

        }
    }

    /**
     * 解析使用 Mapper注解标记的接口
     * @param aClass
     */
    private void parser(Class<?> aClass) {
        if (!aClass.isInterface()) return;
        // TODO 开始解析接口，由接口生成实现类代码
        Method[] methods = aClass.getDeclaredMethods();
        TypeSpec.Builder typeBuilder = createTypeBuilder(aClass);
        for (Method method : methods) {
            MethodSpec methodSpec;
            if (method.isAnnotationPresent(Query.class)) {
                methodSpec = parserQueryMethod(method);
            } else if (method.isAnnotationPresent(Insert.class)) {
                methodSpec = parserInsertMethod(method);
            } else if (method.isAnnotationPresent(Update.class)) {
                methodSpec = parserUpdatetMethod(method);
            } else if (method.isAnnotationPresent(Delete.class)) {
                methodSpec = parserDeleteMethod(method);
            } else {
                methodSpec = parserOtherMethod(method);
            }
            typeBuilder.addMethod(methodSpec);
        }
        TypeSpec implClass = typeBuilder.build();
    }

    /**
     * 创建类生成对象
     * @param aClass
     * @return
     */
    private TypeSpec.Builder createTypeBuilder(Class<?> aClass) {
        // 实现类实现的接口
        ClassName superinterface = ClassName.get(aClass);
        String name = aClass.getSimpleName() + "$1Impl$";
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superinterface);
    }

    /**
     * 解析 @Query 注解标记的方法
     * @param method
     * @return
     */
    private MethodSpec parserQueryMethod(Method method) {
        Query anno = method.getDeclaredAnnotation(Query.class);
        String sql = anno.value();
        Parameter[] params = method.getParameters();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(method.getReturnType());
        for (Parameter param : params) {
            methodBuilder.addParameter(param.getType(), param.getName());
        }
        // TODO 完善查询代码逻辑
        // 主要代码逻辑
        methodBuilder.addCode("$T connection = null;\n" +
                        "$T statement = null;\n" +
                        "try {\n",
                            Connection.class, PreparedStatement.class);
        methodBuilder.addStatement("connection = $T.getInstance().getConnection()", ConnectionHolder.class);
        methodBuilder.addStatement("statement = connection.prepareStatement($S)", sql);
        methodBuilder.addStatement("$T resultSet = statement.executeQuery()", ResultSet.class);
        methodBuilder.addStatement("$T resultSetMetaData = resultSet.getMetaData()", ResultSetMetaData.class);
        methodBuilder.addStatement("int count = resultSetMetaData.getColumnCount()");
        return methodBuilder.build();
    }

    /**
     * 解析 @Insert 注解标记的方法
     * @param method
     * @return
     */
    private MethodSpec parserInsertMethod(Method method) {
        return null;
    }

    /**
     * 解析 @Update 注解标记的方法
     * @param method
     * @return
     */
    private MethodSpec parserUpdatetMethod(Method method) {
        return null;
    }

    /**
     * 解析 @Delete 注解标记的方法
     * @param method
     * @return
     */
    private MethodSpec parserDeleteMethod(Method method) {
        return null;
    }

    /**
     * 解析其他未被注解标记的方法
     * @param method
     * @return
     */
    private MethodSpec parserOtherMethod(Method method) {
        return null;
    }

}
