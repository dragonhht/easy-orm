package com.gitee.carloshuang.processor;

import com.gitee.carloshuang.annotation.*;
import com.gitee.carloshuang.handler.MapperClassLoader;
import com.gitee.carloshuang.handler.MapperProxyInvocationHandler;
import com.gitee.carloshuang.model.QueryResultType;
import com.gitee.carloshuang.model.ResultFieldMessage;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.storage.MapperInstanceStorage;
import com.gitee.carloshuang.storage.QueryResultHolder;
import com.gitee.carloshuang.template.MapperTemplate;
import com.gitee.carloshuang.utils.FileUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.gitee.carloshuang.constant.ParserConstant.JAVA_FILE_PATH;

/**
 * Mapper注解处理器.
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class MapperProcessor {

    /** java文件存放路径. */
    private static final String TEMP_CLASS_PATH;
    /** 包含该注解的类. */
    private Set<Class<?>> classSet;
    /** 实现类全限定名与接口对应关系. */
    private Map<String, Class<?>> implInterfaceMap = new HashMap<>();

    static {
        TEMP_CLASS_PATH = System.getProperty("java.io.tmpdir") + File.separator +
                JAVA_FILE_PATH + System.currentTimeMillis() + File.separator
                .replace(File.separator, "/");
    }

    private MapperProcessor() {
        // 获取有包含Mapper注解的类
        Reflections f = new Reflections("");
        classSet = f.getTypesAnnotatedWith(Mapper.class);
    }

    /**
     * 初始化
     */
    public static void init() {
        new MapperProcessor().process();
    }

    /**
     * 处理带Mapper注解的接口
     */
    private void process() {
        if (CollectionUtils.isEmpty(classSet)) return;
        for (Class<?> aClass : classSet) {
            parser(aClass);
        }
        initInstance();
    }

    /**
     * 解析使用 Mapper注解标记的接口
     * @param aClass
     */
    @SneakyThrows
    private void parser(Class<?> aClass) {
        if (!aClass.isInterface()) return;
        // TODO 开始解析接口，由接口生成实现类代码
        // 包名
        String packageName = aClass.getPackage().getName();
        Method[] methods = aClass.getDeclaredMethods();
        TypeSpec.Builder typeBuilder = createTypeBuilder(aClass);
        String namespace = aClass.getName();
        for (Method method : methods) {
            MethodSpec methodSpec;
            if (method.isAnnotationPresent(Query.class)) {
                methodSpec = QueryMethodProcessor.getProcessor().parserQueryMethod(namespace, method);
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
        // 生成源文件
        JavaFile file = JavaFile.builder(packageName, implClass).build();
        File javaFile = file.writeToFile(new File(TEMP_CLASS_PATH));
        // 编译java文件
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, javaFile.getAbsolutePath());

        // 添加到记录的map中
        implInterfaceMap.put(packageName + "." + implClass.name, aClass);
    }

    /**
     * 初始化生成的实现类实例
     */
    @SneakyThrows
    private void initInstance() {
        // 创建 ClassLoader
        MapperClassLoader classLoader = new MapperClassLoader(
                new URL[]{new URL("file", null, TEMP_CLASS_PATH)});
        for (Map.Entry<String, Class<?>> entry : implInterfaceMap.entrySet()) {
            // 加载 class
            Class<?> implClass = classLoader.loadClass(entry.getKey());
            // 创建实例
            Object impl = implClass.newInstance();
            // 生成代理类
            Object result = new MapperProxyInvocationHandler(impl).getProxy();
            // 放入存储器中
            MapperInstanceStorage.getInstance().put(entry.getValue(), result);
        }
        // 加载完成后删除临时目录
        FileUtils.delDir(new File(TEMP_CLASS_PATH));
    }

    /**
     * 创建类生成对象
     * @param aClass
     * @return
     */
    private TypeSpec.Builder createTypeBuilder(Class<?> aClass) {
        // 实现类实现的接口
        ClassName superinterface = ClassName.get(aClass);
        ClassName mapperTemplate = ClassName.get(MapperTemplate.class);
        String name = aClass.getSimpleName() + "$1Impl$";
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superinterface)
                // 继承模板类
                .superclass(mapperTemplate);
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
