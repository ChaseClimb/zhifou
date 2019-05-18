package com.wenda.controller;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.model.*;
import com.wenda.service.*;
import com.wenda.util.JsoupUtil;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @Autowired
    FeedService feedService;

    @Autowired
    EventProducer eventProducer;


    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        boolean b = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUser().getId()).setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));

        // 返回关注的人数并更新
        return WendaUtil.getJSONString(b ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        boolean b = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        // 返回关注的人数
        return WendaUtil.getJSONString(b ? 0 : 1, String.valueOf( followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)) );
    }


    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Question q = questionService.getQuestionsById(questionId);
        if (q == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        boolean b = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        //关注问题
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId).setEntityOwnerId(q.getUserId()));


        return WendaUtil.getJSONString(b ? 0 : 1);
    }

    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Question q = questionService.getQuestionsById(questionId);
        if (q == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        boolean b = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);



        return WendaUtil.getJSONString(b ? 0 : 1);
    }


    //粉丝列表
    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 100);
        //获取当前用户相关信息
        ViewObject vo = new ViewObject();
        if (hostHolder.getUser() != null) {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
            vo = getLocalUserInfo(hostHolder.getUser().getId(),userId);
        } else {
            model.addAttribute("followers", getUsersInfo(0, followerIds));
            vo = getLocalUserInfo(0,userId);
        }
        model.addAttribute("userInfo", vo);
        return "followers";
    }


    //关注列表
    @RequestMapping(path = {"/user/{uid}/following"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 100);
        //获取当前用户相关信息
        ViewObject vo = new ViewObject();
        if (hostHolder.getUser() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
            vo = getLocalUserInfo(hostHolder.getUser().getId(),userId);
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
            vo = getLocalUserInfo(0,userId);
        }

        model.addAttribute("userInfo", vo);
        return "following";
    }


    private ViewObject getQuestions(Integer userId,Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> questionList = null;
        questionList = questionService.getQuestionsByUserid(userId);

        PageInfo<Question> page = new PageInfo<>(questionList);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            questionList = questionService.getQuestionsByUserid(userId);
            page = new PageInfo<>(questionList);
        }

        List<ViewObject> qVos = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            //去除html标签
            String content = JsoupUtil.noneClean(question.getContent());
            if (content.length() >= 104) {
                content = content.substring(0, 104) + "...";
            }

            question.setContent(content);
            vo.set("question", question);
            qVos.add(vo);
        }

        ViewObject pageVo = new ViewObject();
        pageVo.set("pageNumber", page.getPageNum());
        pageVo.set("totalPage", page.getPages());

        ViewObject vos = new ViewObject();
        //保存问题的相关信息
        vos.set("qVos", qVos);
        //保存分页相关信息
        vos.set("pageVo", pageVo);

        return vos;
    }


    private ViewObject getComments(Integer userId,Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> commentList = null;
        commentList = commentService.getCommentByUserId(userId);

        PageInfo<Comment> page = new PageInfo<>(commentList);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            commentList = commentService.getCommentByUserId(userId);
            page = new PageInfo<>(commentList);
        }

        List<ViewObject> comments = new ArrayList<>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            //去除html标签
            String content = JsoupUtil.noneClean(comment.getContent());
            if (content.length() >= 104) {
                content = content.substring(0, 104) + "...";
            }
            comment.setContent(content);
            Question question = questionService.getQuestionsById(comment.getEntityId());
            vo.set("question", question);
            vo.set("comment", comment);
            comments.add(vo);
        }

        ViewObject pageVo = new ViewObject();
        pageVo.set("pageNumber", page.getPageNum());
        pageVo.set("totalPage", page.getPages());

        ViewObject vos = new ViewObject();
        //保存问题的相关信息
        vos.set("comments", comments);
        //保存分页相关信息
        vos.set("pageVo", pageVo);
        return vos;
    }

    @RequestMapping(path = {"/user/{userId}/asks"}, method = {RequestMethod.GET})
    public String userQuestion(Model model, @PathVariable("userId") Integer userId, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;

        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数就进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }

        //获取问题信息
        ViewObject vos = getQuestions(userId,pageNum, pageSize);
        model.addAttribute("qVos", vos.get("qVos"));
        model.addAttribute("pageVo", vos.get("pageVo"));

        //获取用户相关信息
        ViewObject vo = new ViewObject();
        if (hostHolder.getUser() != null) {
            vo = getLocalUserInfo(hostHolder.getUser().getId(),userId);
        }
        else {
            vo = getLocalUserInfo(0,userId);
        }

        model.addAttribute("userInfo", vo);
        return "userQuestion";
    }


    @RequestMapping(path = {"/user/{userId}/answers"}, method = {RequestMethod.GET})
    public String userComments(Model model, @PathVariable("userId") Integer userId, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;

        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数就进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }

        //获取问题信息
        ViewObject vos = getComments(userId,pageNum, pageSize);
        model.addAttribute("comments", vos.get("comments"));
        model.addAttribute("pageVo", vos.get("pageVo"));

        //获取用户相关信息
        ViewObject vo = new ViewObject();
        if (hostHolder.getUser() != null) {
            vo = getLocalUserInfo(hostHolder.getUser().getId(),userId);
        }
        else {
            vo = getLocalUserInfo(0,userId);
        }
        model.addAttribute("userInfo", vo);
        return "userComment";
    }


    @RequestMapping(path = {"/user/{uid}/activities","/user/{uid}"}, method = {RequestMethod.GET})
    public String activities(Model model, @PathVariable("uid") int userId, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;

        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数就进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Feed> feeds = null;
        feeds = feedService.getFeedsByUserId(userId);

        PageInfo<Feed> page = new PageInfo<>(feeds);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            feeds = feedService.getFeedsByUserId(userId);
            page = new PageInfo<>(feeds);
        }

        model.addAttribute("feeds", feeds);

        ViewObject pageVo = new ViewObject();
        pageVo.set("pageNumber", page.getPageNum());
        pageVo.set("totalPage", page.getPages());

        model.addAttribute("pageVo", pageVo);

        //获取用户相关信息
        ViewObject vo = new ViewObject();
        if (hostHolder.getUser() != null) {
            vo = getLocalUserInfo(hostHolder.getUser().getId(),userId);
        }
        else {
            vo = getLocalUserInfo(0,userId);
        }

        model.addAttribute("userInfo", vo);
        return "userActivity";
    }



    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> userInfos = new ArrayList<ViewObject>();
        for (Integer uid : userIds) {
            User user = userService.getUserById(uid);
            if (user == null) {
                continue;
            }
            ViewObject vo = new ViewObject();
            vo.set("user", user);
            vo.set("commentCount", commentService.getUserCommentCount(uid));
            vo.set("questionCount",questionService.getUserQuestionCount(uid));
            vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, uid));
            vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER));
            if (localUserId != 0) {
                vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, uid));
            } else {
                vo.set("followed", false);
            }
            userInfos.add(vo);
        }
        return userInfos;
    }

    private ViewObject getLocalUserInfo(int localUserId,int userId){
        ViewObject vo = new ViewObject();
        User user = userService.getUserById(userId);
        if (userId != 0) {
            vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("questionCount",questionService.getUserQuestionCount(userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        return vo;
    }



}
