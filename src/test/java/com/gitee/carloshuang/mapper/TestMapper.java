package com.gitee.carloshuang.mapper;

import com.gitee.carloshuang.annotation.Mapper;
import com.gitee.carloshuang.annotation.Query;
import com.gitee.carloshuang.annotation.Result;
import com.gitee.carloshuang.annotation.Results;
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

    @Query("select u.id id from user u where u.id = ?")
    @Results(
            {@Result(column = "id", property = "id")}
    )
    User getUserById(Integer id);

}
