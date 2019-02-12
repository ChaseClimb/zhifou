package com.wenda.service;

import com.wenda.dao.QuestionDao;
import com.wenda.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;

    public List<Question> selectAllQuestions(){
        return questionDao.selectAllQuestions();
    }

    public List<Question> getQuestionsById(Integer userId) {
        return questionDao.getQuestionsById(userId);
    }

    public Integer getUserQuestionCount(Integer userId) {
        return questionDao.getUserQuestionCount(userId);
    }
}
