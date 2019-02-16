package com.wenda.dao;

import com.wenda.model.Message;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface MessageDao {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id,to_id,content,created_date,has_read,conversation_id ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{fromId},#{toId},#{content},#{createdDate},#{hasRead},#{conversationId})"})
    int addMessage(Message message);

    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME,
            " where conversation_id=#{conversationId} order by created_date desc"})
    List<Message> getConversationDetail(String conversationId);

    //复杂的sql语句，子查询先排序 再分组（取最新） 再排序
    @Select({"select ", INSERT_FIELDS, " ,count(id) as id from ( select * from ", TABLE_NAME, " where from_id=#{userId} or to_id=#{userId} order by created_date desc) tt group by conversation_id  order by created_date desc"})
    List<Message> getConversationList(int userId);

    @Select({"select count(id) from ", TABLE_NAME, " where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int getConvesationUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    @Update({"update ",TABLE_NAME," set has_read = 1 where to_id=#{userId} and conversation_id=#{conversationId} "})
    int readMessage(@Param("userId") int localUserId,@Param("conversationId") String conversationId);
}
