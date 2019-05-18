package com.wenda.service;

import com.wenda.dao.CommentDao;
import com.wenda.model.Comment;
import com.wenda.model.Question;
import com.wenda.util.JsoupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDao commentDao;


    public Integer getUserCommentCount(Integer userId) {
        return commentDao.getUserCommentCount(userId);
    }

    public List<Comment> getCommentsByEntity(Integer entityId, Integer entityType) {
        return commentDao.getCommentsByEntity(entityId,entityType);
    }

    public int addComment(Comment comment) {
        return commentDao.addComment(comment);
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDao.getCommentCount(entityId, entityType);
    }

    public int updateComment(Comment comment) {
       /* String result = JsoupUtil.clean(comment.getContent());
        comment.setContent(sensitiveService.filter(result));
*/        return commentDao.updateComment(comment);
    }

    public List<Comment> getCommentByUserId(Integer userId) {
        return commentDao.getCommentByUserId(userId);
    }

    public Comment getCommentById(int id) {
        return commentDao.getCommentById(id);
    }

    public int deleteComment(int commentId) {
        return commentDao.deleteComment(commentId);
    }

    public List<Comment> getByEntityId(Integer id){
        return commentDao.getByEntityId(id);
    }


    public Comment getCommentByIdWithoutStatus(int id) {
        return commentDao.getCommentByIdWithoutStatus(id);
    }

    public int changeStatus(int cid, int status){
        return commentDao.changeStatus(cid,status);
    }
}
