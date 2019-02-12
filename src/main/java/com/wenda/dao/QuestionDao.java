package com.wenda.dao;

import com.wenda.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionDao {
    String TABLE_NAME = " question ";
    String INSERT_FIELDS = " title,content,user_id,created_date,update_date,comment_count,status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME," where status=0 order by created_date desc"})
    List<Question> selectAllQuestions();

    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME," where status=0 and user_id = #{userId}"})
    List<Question> getQuestionsById(Integer userId);

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    Integer getUserQuestionCount(Integer userId);
}