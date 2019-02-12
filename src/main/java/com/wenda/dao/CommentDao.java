package com.wenda.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentDao {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " content,user_id,entity_id,entity_type,created_date,update_date,status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    Integer getUserCommentCount(Integer userId);

}
