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

    public List<Question> selectAllQuestions() {
        return questionDao.selectAllQuestions();
    }

    public List<Question> getQuestionsByUserid(Integer userId) {
        return questionDao.getQuestionsByUserid(userId);
    }

    public Integer getUserQuestionCount(Integer userId) {
        return questionDao.getUserQuestionCount(userId);
    }

    public int addQuestion(Question question) {
        return questionDao.addQuestion(question);
    }

    public Question getQuestionsById(Integer qid) {
        return questionDao.getQuestionsById(qid);
    }

    /**
     * 不带问题状态查询
     *
     * @param qid
     * @return
     */
    public Question getQuestionsByIdWithoutStatus(Integer qid) {
        return questionDao.getQuestionsByIdWithoutStatus(qid);
    }

    public int updateCommentCount(int id, int count) {
        return questionDao.updateCommentCount(id, count);
    }

    public int update(Question question) {
        return questionDao.update(question);
    }

    public int changeStatus(int qid, int status){
        return questionDao.changeStatus(qid,status);
    }

}
