package com.wenda.controller;

import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.HostHolder;
import com.wenda.service.CommentService;
import com.wenda.service.QuestionService;
import com.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class CommentController {
    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;


    @Autowired
    EventProducer eventProducer;

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @RequestMapping(path = "/addComment", method = RequestMethod.POST)
    public String addComment(@RequestParam("questionId") int questionId
            , @RequestParam("edi") String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                return "redirect:/question/" + questionId;
            }
            comment.setStatus(0);
            comment.setCreatedDate(new Date());
            comment.setUpdateDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            //回答问题
            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityType(EntityType.ENTITY_COMMENT).setEntityId(comment.getId()).setEntityOwnerId(comment.getUserId()));

        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }


    @RequestMapping(path = "/updateComment", method = RequestMethod.POST)
    public String updateComment(@RequestParam("commentId") int commentId
            , @RequestParam("questionId") int questionId, @RequestParam("editCk") String content) {
        try {
            Comment comment = new Comment();
            comment.setId(commentId);
            comment.setContent(content);
            if (hostHolder.getUser() == null) {
                return "redirect:/question/" + questionId;
            }
            comment.setUpdateDate(new Date());
            commentService.updateComment(comment);

        } catch (Exception e) {
            logger.error("更新评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }

    @RequestMapping(path = "/deleteComment", method = RequestMethod.POST)
    @ResponseBody
    public String deleteComment(@RequestParam("commentId") int commentId) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            int rowCount = commentService.deleteComment(commentId);
            if (rowCount>0){
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("删除评论失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "删除评论失败");
        }
        return WendaUtil.getJSONString(1, "删除评论失败");
    }


}
