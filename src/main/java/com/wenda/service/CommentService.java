package com.wenda.service;

import com.wenda.dao.CommentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    @Autowired
    CommentDao commentDao;

    public Integer getUserCommentCount(Integer userId) {
        return commentDao.getUserCommentCount(userId);
    }

}
