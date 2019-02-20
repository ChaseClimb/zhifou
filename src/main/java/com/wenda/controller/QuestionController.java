package com.wenda.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.model.*;
import com.wenda.service.*;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/add", method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(String title, String content) {
        try {
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setUpdateDate(new Date());
            question.setCommentCount(0);
            question.setStatus(0);
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            int rowCount = questionService.addQuestion(question);
            if (rowCount > 0) {
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION).
                        setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(question.getId())
                        .setEntityOwnerId(question.getUserId()));
                return WendaUtil.getJSONString(0);
            }

        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }
        return WendaUtil.getJSONString(1, "失败");
    }


    @RequestMapping(value = "/question/{qid}",method = RequestMethod.GET)
    public String questionDetail(Model model,@PathVariable("qid") Integer qid,@RequestParam(value = "page", defaultValue = "1") String pageNumStr){
        Question question = questionService.getQuestionsById(qid);

        model.addAttribute("question", question);
        model.addAttribute("questionLikeCount", likeService.getLikeCount(EntityType.ENTITY_QUESTION,question.getId()));

        ViewObject headInfo = new ViewObject();
        if (hostHolder.getUser() != null) {
            headInfo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            headInfo.set("followed", false);
        }
        headInfo.set("questionOwner",question.getUserId());
        headInfo.set("followCount",followService.getFollowerCount(EntityType.ENTITY_QUESTION, qid));
        model.addAttribute("headInfo",headInfo);

        Integer pageNum = 1;
        Integer pageSize = 20;
        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数才进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);

        PageInfo<Comment> page = new PageInfo<>(commentList);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
            page = new PageInfo<>(commentList);
        }

        ViewObject pageVo = new ViewObject();
        pageVo.set("pageNumber", page.getPageNum());
        pageVo.set("totalPage", page.getPages());

        List<ViewObject> comments = new ArrayList<ViewObject>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);
            vo.set("user", userService.getUserById(comment.getUserId()));

            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));

            comments.add(vo);
        }

        model.addAttribute("comments", comments);
        model.addAttribute("pageVo",pageVo);
        return "detail";
    }

}
