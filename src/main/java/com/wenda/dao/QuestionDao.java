package com.wenda.dao;

import com.wenda.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionDao {
    String TABLE_NAME = " question ";
    String INSERT_FIELDS = " title,content,created_date,update_date,user_id,comment_count,status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where status=0 order by created_date desc"})
    List<Question> selectAllQuestions();

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME})
    List<Question> solrQuestions();

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where status=0 and user_id = #{userId}"})
    List<Question> getQuestionsByUserid(Integer userId);

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    Integer getUserQuestionCount(Integer userId);

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{title},#{content},#{createdDate},#{updateDate},#{userId},#{commentCount},#{status})"})
    int addQuestion(Question question);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where status=0 and id = #{qid}"})
    Question getQuestionsById(Integer qid);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id = #{qid}"})
    Question getQuestionsByIdWithoutStatus(Integer qid);

    @Update({"update ", TABLE_NAME, " set comment_count = #{commentCount} where id=#{id}"})
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    @Update({"update ", TABLE_NAME, " set title = #{title},content=#{content},update_date=#{updateDate} where id=#{id}"})
    int update(Question question);

    int changeStatus(@Param("id")int qid,@Param("status")int status);

}
