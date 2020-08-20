package com.gitee.carloshuang;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.gitee.carloshuang.annotation.Mapper;
import com.gitee.carloshuang.constant.PlatformType;
import com.gitee.carloshuang.model.JdbcConnectionModel;
import com.gitee.carloshuang.model.User;
import com.gitee.carloshuang.storage.ConnectionHolder;
import com.gitee.carloshuang.utils.DataSourceUtils;
import org.junit.Test;
import org.reflections.Reflections;

import javax.annotation.processing.RoundEnvironment;
import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
//        Reflections f = new Reflections("");
//        Set<Class<?>> set = f.getTypesAnnotatedWith(Mapper.class);
//        for (Class<?> aClass : set) {
//            System.out.println(aClass.getName());
//        }
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
        String sql = "select * from user";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = ConnectionHolder.getInstance().getConnection();
            statement = connection.prepareStatement(sql);
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
        try {
            connection = ConnectionHolder.getInstance().getConnection();
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            fillData(User.class, resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException | InstantiationException | IntrospectionException e) {
            e.printStackTrace();
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
        String s = "er.efr.rtrt";
        String[] ss = s.split("\\.");
        System.out.println(Arrays.toString(ss));
    }

}
