package com.gitee.carloshuang.mapper;

import com.gitee.carloshuang.annotation.*;
import com.gitee.carloshuang.model.User;

import java.util.List;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-8-21
 */
@Mapper
public interface TestMapper {

    @Query("select * from user")
    List<User> getUser();

    @Query("select u.id id, u.name name from user u where u.id = ${user.id} and u.name = ${user.name}")
    @Results(
            {@Result(column = "id", property = "id")}
    )
    User getUserById(@Param("user") User user);

    @Insert("insert into user(name, password) value(${user.name}, ${user.password})")
    Integer save(@Param("user") User user);

    @Delete("delete from user where name = ?")
    Integer delete(String name);

    @Update("update user set password = ${user.password} where name = ${user.name}")
    Integer update(@Param("user") User user);

}
