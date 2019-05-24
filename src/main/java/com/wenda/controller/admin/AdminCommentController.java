package com.wenda.controller.admin;

import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.controller.SearchController;
import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.model.admin.AdminHostHolder;
import com.wenda.service.CommentService;
import com.wenda.service.QuestionService;
import com.wenda.service.SensitiveService;
import com.wenda.util.JsoupUtil;
import com.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminCommentController {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommentController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    AdminHostHolder adminHostHolder;

    @RequestMapping(path = {"/admin/comments"}, method = {RequestMethod.GET})
    public String list() {
        if (adminHostHolder.getUser() == null) {
            return "redirect:/admin/login";
        }
        return "/search/comments?qid=-1&q=-1&status=-1";
    }

    @RequestMapping(path = {"/admin/comments/{id}"}, method = {RequestMethod.GET})
    @ResponseBody
    public String get(@PathVariable("id") Integer id) {
        if (adminHostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        Comment comment = commentService.getCommentByIdWithoutStatus(id);
        if (comment == null) {
            return WendaUtil.getJSONString(1, "获取问题详情失败");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("comment", comment);
        return WendaUtil.getJSONString(0, map);
    }


    @RequestMapping(path = "/admin/comments/update", method = RequestMethod.POST)
    public String updateComment(@RequestParam("questionId") int questionId,
                                @RequestParam("commentId") int commentId,
                                @RequestParam("editCk") String content) {
        try {
            if (adminHostHolder.getUser() == null) {
                return "redirect:/admin/login";
            }
            content = sensitiveService.filter(JsoupUtil.clean(content));

            Comment comment = new Comment();
            comment.setId(commentId);
            comment.setContent(content);
            comment.setUpdateDate(new Date());
            int rowCount = commentService.updateComment(comment);

            if (rowCount > 0) {
                eventProducer.fireEvent(new EventModel(EventType.UPDATE_COMMENT).
                        setEntityType(EntityType.ENTITY_COMMENT).
                        setEntityId(comment.getId())
                        .setExt("content", sensitiveService.filter(content)).setExt("status", String.valueOf(0))
                        .setExt("questionId", String.valueOf(questionId)));

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("更新评论失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "更新评论失败");
        }
        return "redirect:/admin/comments";
    }


    @RequestMapping(path = {"/admin/comments/delete/{id}"}, method = {RequestMethod.POST})
    @ResponseBody
    public String delete(@PathVariable("id") Integer id, Integer status, Integer questionId) {
        try {
            if (adminHostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            commentService.changeStatus(id, status);
            int commentCount = questionService.getQuestionsById(questionId).getCommentCount();
            int rowCount = questionService.updateCommentCount(questionId, commentCount - 1);
            if (rowCount > 0) {
                Comment comment = commentService.getCommentByIdWithoutStatus(id);
                if (comment == null) {
                    throw new RuntimeException("删除问题失败");
                }

                eventProducer.fireEvent(new EventModel(EventType.CHANGE_STATUS).
                        setEntityType(EntityType.ENTITY_COMMENT).
                        setEntityId(comment.getId())
                        .setExt("content", comment.getContent()).setExt("status", String.valueOf(status)).
                                setExt("questionId", String.valueOf(questionId)));
                Thread.sleep(1000);
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("删除问题失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "删除问题失败");

        }
        return WendaUtil.getJSONString(1, "失败");
    }

    @RequestMapping(path = {"/admin/comments/recover/{id}"}, method = {RequestMethod.POST})
    @ResponseBody
    public String recover(@PathVariable("id") Integer id, Integer status, Integer questionId) {
        try {
            if (adminHostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            int rowCount = commentService.changeStatus(id, status);
            if (rowCount > 0) {
                Comment comment = commentService.getCommentByIdWithoutStatus(id);
                if (comment == null) {
                    throw new RuntimeException("恢复评论失败");
                }
                eventProducer.fireEvent(new EventModel(EventType.CHANGE_STATUS).
                        setEntityType(EntityType.ENTITY_COMMENT).
                        setEntityId(comment.getId())
                        .setExt("content", comment.getContent()).setExt("status", String.valueOf(status))
                        .setExt("questionId", String.valueOf(questionId)));
                Thread.sleep(1000);
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("恢复评论失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "恢复评论失败");
        }
        return WendaUtil.getJSONString(1, "失败");
    }

}
