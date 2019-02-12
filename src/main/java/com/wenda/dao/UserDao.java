package com.wenda.dao;

import com.wenda.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {
    String TABLE_NAME = " user ";
    String INSERT_FIELDS = " name,password,salt,head_url,signature ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    User getUserById(int id);
}
