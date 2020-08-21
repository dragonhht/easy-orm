package com.gitee.carloshuang.mapper;

import com.gitee.carloshuang.annotation.Mapper;
import com.gitee.carloshuang.annotation.Query;
import com.gitee.carloshuang.annotation.Result;
import com.gitee.carloshuang.annotation.Results;
import com.gitee.carloshuang.model.User;

/**
 * .
 *
 * @author: Carlos Huang
 * @Date: 2020-8-21
 */
@Mapper
public interface TestMapper {

    @Query("select * from user")
    User getUser();

    @Query("select u.id id from user u")
    @Results(
            {@Result(column = "id", property = "id")}
    )
    User getUserBuId(Integer id);

}
