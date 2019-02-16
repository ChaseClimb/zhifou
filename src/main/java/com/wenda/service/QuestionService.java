package com.wenda.service;

import com.wenda.dao.QuestionDao;
import com.wenda.model.Question;
import com.wenda.util.JsoupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;

    @Autowired
    SensitiveService sensitiveService;

    public List<Question> selectAllQuestions(){
        return questionDao.selectAllQuestions();
    }

    public List<Question> getQuestionsByUserid(Integer userId) {
        return questionDao.getQuestionsByUserid(userId);
    }

    public Integer getUserQuestionCount(Integer userId) {
        return questionDao.getUserQuestionCount(userId);
    }

    public int addQuestion(Question question) {
        //对html标签过滤
        question.setContent(JsoupUtil.noneClean(question.getContent()));
        question.setTitle(JsoupUtil.noneClean(question.getTitle()));
        //敏感词过滤
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));
        return questionDao.addQuestion(question);
    }

    public Question getQuestionsById(Integer qid) {
        return questionDao.getQuestionsById(qid);
    }

    public int updateCommentCount(int id, int count) {
        return questionDao.updateCommentCount(id, count);
    }
}
