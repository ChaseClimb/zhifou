package com.wenda.async.handler;

import com.wenda.async.EventHandler;
import com.wenda.async.EventModel;
import com.wenda.async.EventType;
import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.service.CommentService;
import com.wenda.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class IndexHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(IndexHandler.class);
    @Autowired
    SearchService searchService;

    @Autowired
    CommentService commentService;


    @Override
    public void doHandle(EventModel model) {
        try {
            //新增或更新评论
            if (model.getType() != EventType.CHANGE_STATUS) {
                if (model.getEntityType() == 1) {
                    Question question = new Question();
                    question.setId(model.getEntityId());
                    question.setTitle(model.getExt("title"));
                    question.setContent(model.getExt("content"));
                    question.setStatus(Integer.parseInt(model.getExt("status")));
                    question.setUpdateDate(new Date());
                    searchService.saveOrUpdateIndex(question, model.getEntityType());
                } else if (model.getEntityType() == 2) {
                    Comment comment = new Comment();
                    comment.setId(model.getEntityId());
                    comment.setContent(model.getExt("content"));
                    comment.setStatus(Integer.parseInt(model.getExt("status")));
                    comment.setEntityId(Integer.parseInt(model.getExt("questionId")));
                    comment.setUpdateDate(new Date());
                    searchService.saveOrUpdateIndex(comment, model.getEntityType());
                }
            } else {
                if (model.getEntityType() == 1) {
                    List<Question> questionList = new ArrayList<>();
                    List<Comment> commentList = new ArrayList<>();
                    Question question = new Question();
                    question.setId(model.getEntityId());
                    question.setTitle(model.getExt("title"));
                    question.setContent(model.getExt("content"));
                    question.setStatus(Integer.parseInt(model.getExt("status")));
                    question.setUpdateDate(new Date());
                    questionList.add(question);
                    searchService.batchDeleteIndex(questionList, model.getEntityType());

                    commentList = commentService.getByEntityId(model.getEntityId());
                    List<Comment> comments = new ArrayList<>();
                    Date date = new Date();
                    for (Comment comment : commentList) {
                        comment.setStatus(Integer.parseInt(model.getExt("status")));
                        comment.setUpdateDate(date);
                        comments.add(comment);
                    }
                    searchService.batchDeleteIndex(comments, EntityType.ENTITY_COMMENT);
                } else if (model.getEntityType() == 2) {
                    List<Comment> commentList = new ArrayList<>();
                    Comment comment = new Comment();
                    comment.setId(model.getEntityId());
                    comment.setContent(model.getExt("content"));
                    comment.setStatus(Integer.parseInt(model.getExt("status")));
                    comment.setEntityId(Integer.parseInt(model.getExt("questionId")));
                    comment.setUpdateDate(new Date());
                    commentList.add(comment);
                    searchService.batchDeleteIndex(commentList, EntityType.ENTITY_COMMENT);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("更新索引失败");
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(
                EventType.ADD_QUESTION, EventType.UPDATE_QUESTION,
                EventType.COMMENT, EventType.UPDATE_COMMENT,
                EventType.CHANGE_STATUS
        );
    }


}
