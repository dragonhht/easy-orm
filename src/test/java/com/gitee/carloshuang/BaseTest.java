package com.gitee.carloshuang;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.gitee.carloshuang.mapper.TestMapper;
import com.gitee.carloshuang.model.QueryResultType;
import com.gitee.carloshuang.model.ResultFieldMessage;
import com.gitee.carloshuang.model.User;
import com.gitee.carloshuang.processor.MapperProcessor;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.storage.MapperInstanceStorage;
import com.gitee.carloshuang.storage.QueryResultHolder;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-8-17
 */
public class BaseTest {

    @Test
    public void testGetAnno() {
        String sql = "select * from user where n =? and s in (:good ) and id = :userId and name = :name and id = ?";
//        System.out.println(QueryMethodProcessor.parserSqlParam(sql));
    }

    @Test
    public void testReflect() throws IntrospectionException {
        User user = new User();
//        MethodAccess methodAccess = MethodAccess.get(User.class);
//        methodAccess.invoke(user, "setId", 23);
//        methodAccess.invoke(user, "setName", "Carlos Huang");
//        System.out.println(user);
        Field[] fields = User.class.getDeclaredFields();
        long start = System.currentTimeMillis();
        for (Field name : fields) {
            PropertyDescriptor descriptor = new PropertyDescriptor(name.getName(), User.class);
            Method writeMethod = descriptor.getWriteMethod();
            System.out.println(writeMethod.getName());
        }
        System.out.println("用时: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testDataSource() {
        String sql = "select * from user where id in (?)";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionHolder.getInstance().getConnection();
            statement = connection.prepareStatement(sql);
            Array array = connection.createArrayOf("int", new Object[] {1, 2});
            statement.setArray(1, array);
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i < count; i++) {
                    String name = resultSetMetaData.getColumnLabel(i);
                    System.out.print(name + "=" + resultSet.getString(name) + "; ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            ConnectionHolder.getInstance().closeConnection();
        }
    }

    @Test
    public void testQuery() {
        String sql = "select * from user";
        Connection connection = null;
        PreparedStatement statement = null;
        Object result = null;
        try {
            connection = ConnectionHolder.getInstance().getConnection();
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            Map<String, ResultFieldMessage> resultFieldMessageMap = QueryResultHolder.getInstance().getResultMap("");
            QueryResultType resultType = QueryResultHolder.getInstance().getResultType("");
        }catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throw new RuntimeException(throwables);
                }
            }
            ConnectionHolder.getInstance().closeConnection();
        }
        User.class.cast(result);
    }

    private void fillData(Class<?> returnType, ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, IntrospectionException {
        Field[] fields = returnType.getDeclaredFields();
        Map<String, String> fieldMap = new HashMap<>();
        for (Field field : fields) {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), returnType);
            Method writeMethod = descriptor.getWriteMethod();
            fieldMap.put(field.getName(), writeMethod.getName());
        }
        MethodAccess methodAccess = MethodAccess.get(returnType);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int count = resultSetMetaData.getColumnCount();
        while (resultSet.next()) {
            Object target = returnType.newInstance();
            for (int i = 1; i < count; i++) {
                String name = resultSetMetaData.getColumnLabel(i);
                if (fieldMap.containsKey(name)) {
                    String valueField = fieldMap.get(name);
                    methodAccess.invoke(target, valueField, resultSet.getObject(name));
                }
            }
            System.out.println("结果: " + target);
        }
        resultSet.close();
    }

    @Test
    public void testThreadConnection() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                Connection connection = ConnectionHolder.getInstance().getConnection();
                System.out.println(connection);
                ConnectionHolder.getInstance().closeConnection();
            }).start();
        }
        TimeUnit.MINUTES.sleep(1);
    }

    @Test
    public void te() {
        MapperProcessor.init();
        TestMapper mapper = MapperInstanceStorage.getInstance().get(TestMapper.class);
        List<User> user = mapper.getUser();
        System.out.println(user);
        User u = mapper.getUserById(1);
        System.out.println("getById: " + u);
    }

    @Test
    public void test() {
        Class<?> type = List.class;
        User[] users = new User[0];
        String s = "";
        System.out.println("是否为字符串类型: " + s.getClass().equals(String.class));
        System.out.println("是否基本类型: " + double.class.isPrimitive());
        System.out.println("是否为集合: " + Collection.class.isAssignableFrom(type));
        System.out.println("是否为数组: " + users.getClass().isArray());
        Method[] methods = Te.class.getDeclaredMethods();
        for (Method method : methods) {
            Class<?> re = method.getReturnType();
            if (Collection.class.isAssignableFrom(re)) {
                Type returnType = method.getGenericReturnType();
                Type subType =  ((ParameterizedType) returnType).getActualTypeArguments()[0];
                if (subType instanceof  ParameterizedType) {
                    Class<?> t = (Class<?>) ((ParameterizedType) subType).getRawType();
                    System.out.println("是否为Map: " + Map.class.isAssignableFrom(t));
                    Type[] sus = ((ParameterizedType) subType).getActualTypeArguments();
                    System.out.println(Arrays.toString(sus));
                    System.out.println(subType);
                    continue;
                }
                Class<?> clzz = (Class<?>) subType;
                if (clzz.isArray()) System.out.println("数组");
            }
        }
        int[] us = new int[0];
        System.out.println(us.getClass().getComponentType());
    }

}
