package com.wenda.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.wenda.async.EventHandler;
import com.wenda.async.EventModel;
import com.wenda.async.EventType;
import com.wenda.model.*;
import com.wenda.service.*;
import com.wenda.util.JedisAdapter;
import com.wenda.util.JsoupUtil;
import com.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    Map<String, String> map = new HashMap<String, String>();

    private String buildFeedData(EventModel model) {

        User owner = userService.getUserById(model.getEntityOwnerId());
        if (owner == null) {
            return null;
        }

        map.put("ownerId", String.valueOf(owner.getId()));
        map.put("ownerHead", owner.getHeadUrl());
        map.put("ownerSignature", owner.getSignature());
        map.put("ownerName", owner.getName());
        if (model.getType() == EventType.LIKE_QUESTION) {
            getQuestionInfo(model, map);
            return JSONObject.toJSONString(map);
        } else if (model.getType() == EventType.LIKE_COMMENT) {
            Comment comment = commentService.getCommentById(model.getEntityId());
            if (comment == null) {
                return null;
            }
            //去除html标签
            String content = JsoupUtil.noneClean(comment.getContent());
            if (content.length() >= 104) {
                content = content.substring(0, 104) + "...";
            }
            Question question = questionService.getQuestionsById(comment.getEntityId());
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            map.put("commentContent", content);
            return JSONObject.toJSONString(map);
        } else if (model.getType() == EventType.ADD_QUESTION) {
            getQuestionInfo(model, map);
            return JSONObject.toJSONString(map);
        } else if (model.getType() == EventType.COMMENT) {
            Comment comment = commentService.getCommentById(model.getEntityId());
            if (comment == null) {
                return null;
            }
            //去除html标签
            String content = JsoupUtil.noneClean(comment.getContent());
            if (content.length() >= 104) {
                content = content.substring(0, 104) + "...";
            }
            Question question = questionService.getQuestionsById(comment.getEntityId());
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            map.put("commentContent", content);
            return JSONObject.toJSONString(map);
        } else if (model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION) {
            getQuestionInfo(model, map);
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    private void getQuestionInfo(EventModel model, Map<String, String> map) {
        Question question = questionService.getQuestionsById(model.getEntityId());
        String questionContent = JsoupUtil.noneClean(question.getContent());
        if (questionContent.length() >= 104) {
            questionContent = questionContent.substring(0, 104) + "...";
        }
        map.put("questionId", String.valueOf(question.getId()));
        map.put("questionTitle", question.getTitle());
        map.put("questionContent", questionContent);
    }

    @Override
    public void doHandle(EventModel model) {
        // 构造一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedData(model));
        if (feed.getData() == null) {
            // 不支持的feed
            return;
        }
        feedService.addFeed(feed);
    }

    /**
     * 添加问题、评论、点赞问题、点赞评论、关注
     * @return
     */
    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.LIKE_QUESTION, EventType.LIKE_COMMENT, EventType.COMMENT, EventType.FOLLOW, EventType.ADD_QUESTION});
    }
}
