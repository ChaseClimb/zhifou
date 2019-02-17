package com.wenda.service;

import com.wenda.dao.CommentDao;
import com.wenda.model.Comment;
import com.wenda.util.JsoupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDao commentDao;

    @Autowired
    SensitiveService sensitiveService;

    public Integer getUserCommentCount(Integer userId) {
        return commentDao.getUserCommentCount(userId);
    }

    public List<Comment> getCommentsByEntity(Integer entityId, Integer entityType) {

        return commentDao.getCommentsByEntity(entityId,entityType);
    }

    public int addComment(Comment comment) {

        //comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        String result = JsoupUtil.clean(comment.getContent());
        comment.setContent(sensitiveService.filter(result));
        return commentDao.addComment(comment);
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDao.getCommentCount(entityId, entityType);
    }

    public int updateComment(Comment comment) {
        String result = JsoupUtil.clean(comment.getContent());
        comment.setContent(sensitiveService.filter(result));
        return commentDao.updateComment(comment);
    }
}
