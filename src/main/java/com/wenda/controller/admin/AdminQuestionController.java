package com.wenda.controller.admin;

import com.wenda.async.EventModel;
import com.wenda.async.EventProducer;
import com.wenda.async.EventType;
import com.wenda.controller.SearchController;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.model.admin.AdminHostHolder;
import com.wenda.service.QuestionService;
import com.wenda.service.SensitiveService;
import com.wenda.util.JsoupUtil;
import com.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminQuestionController {
    private static final Logger logger = LoggerFactory.getLogger(AdminQuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    AdminHostHolder adminHostHolder;

    @RequestMapping(path = "/admin", method = {RequestMethod.GET})
    public String admin() {
        if (adminHostHolder.getUser()==null){
            return "redirect:/admin/login";
        }
        return "redirect:admin/questions";
    }

    @RequestMapping(path = {"/admin/questions"})
    public String list() {
        if (adminHostHolder.getUser()==null){
            return "redirect:/admin/login";
        }

        return "/search/questions?qid=-1&q=-1&status=-1";
    }

    @RequestMapping(path = {"/admin/questions/{id}"}, method = {RequestMethod.GET})
    @ResponseBody
    public Object get(@PathVariable("id") Integer id) {
        if (adminHostHolder.getUser()==null){
            return WendaUtil.getJSONString(999);
        }
        Question question = questionService.getQuestionsByIdWithoutStatus(id);
        if (question == null) {
            return WendaUtil.getJSONString(1, "获取问题详情失败");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("question", question);
        return WendaUtil.getJSONString(0, map);
    }

    @RequestMapping(path = {"/admin/questions/update/{id}"}, method = {RequestMethod.POST})
    @ResponseBody
    public String update(@PathVariable("id") int id, String title, String content) {
        try {
            if (adminHostHolder.getUser()==null){
                return WendaUtil.getJSONString(999);
            }
            Question question = new Question();

            question.setId(id);
            //对标题和内容进行过滤
            String cleanContent = sensitiveService.filter(JsoupUtil.noneClean(content));
            String cleanTitle = sensitiveService.filter(JsoupUtil.noneClean(title));
            question.setTitle(cleanTitle);
            question.setContent(cleanContent);

            Date date = new Date();
            question.setUpdateDate(date);

            int rowCount = questionService.update(question);
            if (rowCount > 0) {
                eventProducer.fireEvent(new EventModel(EventType.UPDATE_QUESTION).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(question.getId())
                        .setExt("title", cleanTitle).setExt("content", cleanContent).setExt("status", String.valueOf(0)));

                Thread.sleep(1000);
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("修改问题失败" + e.getMessage());
        }
        return WendaUtil.getJSONString(1, "修改问题失败");
    }

    @RequestMapping(path = {"/admin/questions/delete/{id}"}, method = {RequestMethod.POST})
    @ResponseBody
    public String delete(@PathVariable("id") Integer id, Integer status) {
        try {
            if (adminHostHolder.getUser()==null){
                return WendaUtil.getJSONString(999);
            }
            int rowCount = questionService.changeStatus(id, status);
            if (rowCount > 0) {
                Question question = questionService.getQuestionsByIdWithoutStatus(id);
                String cleanContent = JsoupUtil.noneClean(question.getContent());
                String cleanTitle = JsoupUtil.noneClean(question.getTitle());
                eventProducer.fireEvent(new EventModel(EventType.CHANGE_STATUS).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(question.getId())
                        .setExt("title", cleanTitle).setExt("content", cleanContent).setExt("status", String.valueOf(status)));
                Thread.sleep(1000);
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("删除问题失败" + e.getMessage());
        }
        return WendaUtil.getJSONString(1, "删除问题失败");
    }


    @RequestMapping(path = {"/admin/questions/recover/{id}"}, method = {RequestMethod.POST})
    @ResponseBody
    public String recover(@PathVariable("id") Integer id, Integer status) {
        try {
            if (adminHostHolder.getUser()==null){
                return WendaUtil.getJSONString(999);
            }
            int rowCount = questionService.changeStatus(id, status);
            if (rowCount > 0) {
                Question question = questionService.getQuestionsByIdWithoutStatus(id);
                String cleanContent = JsoupUtil.noneClean(question.getContent());
                String cleanTitle = JsoupUtil.noneClean(question.getTitle());
                eventProducer.fireEvent(new EventModel(EventType.CHANGE_STATUS).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(question.getId())
                        .setExt("title", cleanTitle).setExt("content", cleanContent).setExt("status", String.valueOf(status)));
                Thread.sleep(1000);
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("恢复问题失败" + e.getMessage());
        }
        return WendaUtil.getJSONString(1, "恢复问题失败");
    }

}
