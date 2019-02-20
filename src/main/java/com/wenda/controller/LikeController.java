package com.wenda.controller;

import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.HostHolder;
import com.wenda.model.Question;
import com.wenda.service.CommentService;
import com.wenda.service.LikeService;
import com.wenda.service.QiniuService;
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

@Controller
public class LikeController {
    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;


    @Autowired
    QuestionService questionService;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }

            //点赞问题
            if (entityType==1){
                Question question = questionService.getQuestionsById(entityId);
                eventProducer.fireEvent(new EventModel(EventType.LIKE_QUESTION).
                        setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(entityId)
                        .setEntityOwnerId(question.getUserId()));
            }
            //点赞回答
            if (entityType == 2) {
                Comment comment = commentService.getCommentById(entityId);
                eventProducer.fireEvent(new EventModel(EventType.LIKE_COMMENT).
                        setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_COMMENT).setEntityId(entityId)
                        .setEntityOwnerId(comment.getUserId()));
            }

            long likeCount = likeService.like(hostHolder.getUser().getId(), entityType, entityId);


            return WendaUtil.getJSONString(0, String.valueOf(likeCount));
        } catch (Exception e) {
            logger.error("点赞失败", e.getMessage());
            return WendaUtil.getJSONString(1, "点赞失败");
        }
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }

            long dislikeCount = likeService.dislike(hostHolder.getUser().getId(), entityType, entityId);
            return WendaUtil.getJSONString(0, String.valueOf(dislikeCount));
        } catch (Exception e) {
            logger.error("点踩失败", e.getMessage());
            return WendaUtil.getJSONString(1, "点踩失败");
        }
    }

    @RequestMapping(path = {"/cancelFollow"}, method = {RequestMethod.POST})
    @ResponseBody
    public String cancelFollow(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            long likeCount = likeService.cancelFollow(hostHolder.getUser().getId(), entityType, entityId);
            return WendaUtil.getJSONString(0, String.valueOf(likeCount));
        } catch (Exception e) {
            logger.error("点赞失败", e.getMessage());
            return WendaUtil.getJSONString(1, "点赞失败");
        }
    }

    @RequestMapping(path = {"/cancelUnfollow"}, method = {RequestMethod.POST})
    @ResponseBody
    public String cancelUnfollow(@RequestParam("entityType") int entityType, @RequestParam("entityId") int entityId) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            likeService.cancelUnFollow(hostHolder.getUser().getId(), entityType, entityId);
            return WendaUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("点赞失败", e.getMessage());
            return WendaUtil.getJSONString(1, "点赞失败");
        }
    }


}
