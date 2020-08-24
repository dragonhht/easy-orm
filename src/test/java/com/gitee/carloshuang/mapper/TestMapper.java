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

    @Query("select u.id id, u.name name from user u where u.id = ${id}")
    @Results(
            {@Result(column = "id", property = "id")}
    )
    User getUserById(@Param("id") Integer id);

//    @Insert("insert into user value(?, ?, ?)")
//    Integer save(User user);

}
