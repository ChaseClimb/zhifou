package com.wenda.dao;

import com.wenda.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentDao {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " content,user_id,entity_id,entity_type,created_date,update_date,status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    Integer getUserCommentCount(Integer userId);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where entity_id=#{entityId} and entity_type=#{entityType} and status=0 order by created_date asc"})
    List<Comment> getCommentsByEntity(@Param("entityId") Integer entityId, @Param("entityType") Integer entityType);

    @Insert({"insert into ", TABLE_NAME, " (", INSERT_FIELDS, ") values (#{content},#{userId},#{entityId},#{entityType},#{createdDate},#{updateDate},#{status})"})
    int addComment(Comment comment);

    @Select({"select count(id) from ", TABLE_NAME, " where entity_id=#{entityId} and entity_type=#{entityType} and status=0"})
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);

    @Update({"update ", TABLE_NAME, " set content=#{content},update_date=#{updateDate} where id=#{id}"})
    int updateComment(Comment comment);
}
