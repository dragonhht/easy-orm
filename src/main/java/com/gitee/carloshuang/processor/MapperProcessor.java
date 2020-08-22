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

    private static MapperProcessor PROCESSOR;
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
                methodSpec = parserQueryMethod(namespace, method);
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
     * 解析 @Query 注解标记的方法
     * @param namespace 命名空间
     * @param method
     * @return
     */
    private MethodSpec parserQueryMethod(String namespace, Method method) {
        // 解析查询结果字段映射
        String methodId = parserResultMap(namespace, method);
        // 解析 Query
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
                        "$T result = null;\n" +
                        "try {\n",
                            Connection.class, PreparedStatement.class, method.getReturnType());
        methodBuilder.addStatement("connection = $T.getInstance().getConnection()", ConnectionHolder.class);
        methodBuilder.addStatement("statement = connection.prepareStatement($S)", sql);
        methodBuilder.addStatement("$T resultSet = statement.executeQuery()", ResultSet.class);
        methodBuilder.addStatement("$T<String, $T> resultFieldMessageMap = $T.getInstance().getResultMap($S)",
                Map.class, ResultFieldMessage.class, QueryResultHolder.class, methodId);
        methodBuilder.addStatement("$T resultType = QueryResultHolder.getInstance().getResultType($S)",
                QueryResultType.class, methodId);
        methodBuilder.addStatement("result = ($T) fillData(resultType, resultFieldMessageMap, resultSet)", method.getReturnType());
        methodBuilder.addCode("}catch ($T e) {\n" +
                "            throw new $T(e);\n" +
                "        } finally {\n" +
                "            if (statement != null) {\n" +
                "                try {\n" +
                "                    statement.close();\n" +
                "                } catch ($T throwables) {\n" +
                "                    throw new RuntimeException(throwables);\n" +
                "                }\n" +
                "            }\n" +
                "            ConnectionHolder.getInstance().closeConnection();\n" +
                "        }\n",
                Exception.class, RuntimeException.class, SQLException.class);
        methodBuilder.addStatement("return result");
        return methodBuilder.build();
    }

    /**
     * 解析 @Results 注解
     * @param namespace 命名空间
     * @param method
     * @return 方法id
     */
    private String parserResultMap(String namespace, Method method) {
        Map<String, String> map = new HashMap<>();
        // 默认id为方法名
        String id = method.getName();
        Results annotation = method.getDeclaredAnnotation(Results.class);
        if (annotation != null) {
            String nameId = annotation.id();
            // 如果用户自定义id，则使用用户自定义id
            if (StringUtils.isNotEmpty(nameId)) id = nameId;
            Result[] results = annotation.value();
            for (Result result : results) {
                map.put(result.column(), result.property());
            }
        }
        // 保存
        String methodId = namespace + "." + id;
        // 解析方法返回类型
        QueryResultType queryResultType = parserQueryMethodReturn(method.getReturnType(), method.getGenericReturnType());
        // 保存方法返回类型
        QueryResultHolder.getInstance().putResultType(methodId, queryResultType);
        // 解析查询结果字段与实体对应关系
        Map<String, ResultFieldMessage> resultMap = resultMap(queryResultType, map);
        QueryResultHolder.getInstance().putFieldMap(methodId, resultMap);
        return methodId;
    }

    /**
     * 解析方法返回类型信息
     * @return
     */
    private QueryResultType parserQueryMethodReturn(Class<?> returnType, Type type) {
        QueryResultType resultType = new QueryResultType();
        resultType.setReturnType(returnType);
        // 是否为基本类型
        if (resultType.isPrimitive()) {
            resultType.setPrimitive(true);
            return resultType;
        } else {
            // 判断是否为字符串、Date或和转为基本类型
            if (returnType.equals(String.class) || returnType.equals(Date.class) || returnType.equals(Double.class)
                    || returnType.equals(Float.class) || returnType.equals(Long.class) || returnType.equals(Integer.class)
                    || returnType.equals(Boolean.class)) {
                resultType.setPrimitive(true);
                return resultType;
            }
        }
        // 集合类型
        if (Collection.class.isAssignableFrom(returnType)) {
            if (!List.class.isAssignableFrom(returnType) && !Set.class.isAssignableFrom(returnType))
                throw new RuntimeException("暂时只支持List或Set");
            resultType.setCollection(true);
            // 查询泛型类型
            parserGenericType(type, resultType);
        }
        // 数组类型
        if (returnType.isArray()) {
            resultType.setArray(true);
        }
        return resultType;
    }

    /**
     * 处理数组类型
     * @param resultType
     */
    private void parserArrayType(Class<?> type, QueryResultType resultType) {
        Class<?> clazz = type.getComponentType();
        resultType.setReturnType(clazz);
    }

    /**
     * 处理集合和Map类型
     * @param type
     */
    private void parserGenericType(Type type, QueryResultType resultType) {
        Type[] subTypes = ((ParameterizedType) type).getActualTypeArguments();
        Class<?>[] subTypeClasses = new Class<?>[subTypes.length];
        for (int i = 0; i < subTypes.length; i++) {
            Type subType = subTypes[i];
            // 判断是否为参数化类型
            if (subType instanceof ParameterizedType) {
                // 获取类型
                Class<?> clazz = (Class<?>) ((ParameterizedType) subType).getRawType();
                // 是否为Map类型
                if (!Map.class.isAssignableFrom(clazz)) throw new RuntimeException("暂时只支持Collection<Map>多级泛型参数");
                QueryResultType subResultType = new QueryResultType();
                subResultType.setReturnType(clazz);
                subResultType.setMap(true);
                resultType.setSubResultType(subResultType);
                subTypeClasses[i] = clazz;
                continue;
            }
            Class<?> clazz = (Class<?>) subType;
            if (!clazz.isArray()) {
                subTypeClasses[i] = clazz;
                QueryResultType subResultType = parserQueryMethodReturn(clazz, null);
                resultType.setSubResultType(subResultType);
                continue;
            }
            QueryResultType subResultType = new QueryResultType();
            subResultType.setArray(true);
            parserArrayType(clazz, subResultType);
            resultType.setSubResultType(subResultType);
        }
        resultType.setSubType(subTypeClasses);
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


    /**
     * 组装查询结果字段映射关系
     * @param queryResultType 返回类型
     * @param resultMapSettings 用户自定义的映射关系
     * @return
     */
    @SneakyThrows
    private Map<String, ResultFieldMessage> resultMap(QueryResultType queryResultType, Map<String, String> resultMapSettings) {
        Map<String, ResultFieldMessage> resultMap = new HashMap<>();
        Class<?> returnType = null;
        // 是实体模型或集合下为实体模型是才解析
        if (isModel(queryResultType) ) {
            returnType = queryResultType.getReturnType();
            Map<String, ResultFieldMessage> settingResultMap = handlerResultMapSetting(returnType, resultMapSettings);
            resultMap.putAll(settingResultMap);
        } else if ((queryResultType.isCollection() && isModel(queryResultType.getSubResultType()))) {
            returnType = queryResultType.getSubType()[0];
            Map<String, ResultFieldMessage> settingResultMap =
                    handlerResultMapSetting(returnType, resultMapSettings);
            resultMap.putAll(settingResultMap);
        } else {
            // 没有模型则直接返回不进行下一步解析
            return resultMap;
        }
        // 实体其余字段按属性名对应
        Field[] fields = returnType.getDeclaredFields();
        for (Field field : fields) {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), returnType);
            Method writeMethod = descriptor.getWriteMethod();
            if (!resultMap.containsKey(field.getName())) {
                ResultFieldMessage message = new ResultFieldMessage(writeMethod.getName());
                resultMap.put(field.getName(), message);
            }
        }
        return resultMap;
    }

    /**
     * 判断返回结果类型是否为实体模型
     * @param resultType
     * @return
     */
    private boolean isModel(QueryResultType resultType) {
        if (resultType == null) return false;
        return !resultType.isArray() && !resultType.isCollection() && !resultType.isMap() && !resultType.isPrimitive();
    }

    /**
     * 处理用户自定义的映射关系
     * @param returnType 接收实体类型
     * @param resultMapSettings 自定义字段映射
     */
    private Map<String, ResultFieldMessage> handlerResultMapSetting(Class<?> returnType,
                                                                    Map<String, String> resultMapSettings) {
        Map<String, ResultFieldMessage> map = new HashMap<>();
        for (Map.Entry<String, String> entry : resultMapSettings.entrySet()) {
            String value = entry.getValue();
            String[] fields = value.split("\\.");
            // 一级时
            if (fields.length < 2) {
                try {
                    PropertyDescriptor descriptor = new PropertyDescriptor(value, returnType);
                    Method writeMethod = descriptor.getWriteMethod();
                    ResultFieldMessage message = new ResultFieldMessage(writeMethod.getName());
                    map.put(entry.getKey(), message);
                    continue;
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            // 存放对应的方法
            String[] methods = new String[fields.length];
            Class<?> modelType = returnType;
            int last = fields.length - 1;
            // 读方法与写方法的映射
            Map<String, String> getSetMap = new HashMap<>();
            Map<String, Class<?>> readTypeMap = new HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                try {
                    if (i != last) {
                        // 不是最后一个属性，则获取 get 方法
                        PropertyDescriptor descriptor = new PropertyDescriptor(fields[i], modelType);
                        Method readMethod = descriptor.getReadMethod();
                        modelType = readMethod.getReturnType();
                        methods[i] = readMethod.getName();
                        // 写方法
                        Method writeMethod = descriptor.getWriteMethod();
                        getSetMap.put(methods[i], writeMethod.getName());
                        readTypeMap.put(methods[i], writeMethod.getReturnType());
                        continue;
                    }
                    // 最后一个属性获取 set 方法
                    PropertyDescriptor descriptor = new PropertyDescriptor(fields[i], modelType);
                    Method writeMethod = descriptor.getWriteMethod();
                    methods[i] = writeMethod.getName();
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            value = StringUtils.join(methods, ".");
            ResultFieldMessage message = new ResultFieldMessage(value);
            message.setReadWriteMap(getSetMap);
            message.setReadTypeMap(readTypeMap);
            map.put(entry.getKey(), message);
        }
        return map;
    }

}
