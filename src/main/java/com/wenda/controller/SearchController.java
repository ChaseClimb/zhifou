package com.wenda.controller;

import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.model.ViewObject;
import com.wenda.service.FollowService;
import com.wenda.service.QuestionService;
import com.wenda.service.SearchService;
import com.wenda.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    SearchService searchService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @RequestMapping(path = {"/search"}, method = {RequestMethod.GET})
    public String search(Model model, @RequestParam("q") String keyword,
                         @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        try {
            Integer pageNum = 1;
            Integer pageSize = 10;
            if (StringUtils.isBlank(keyword)) {
                return "redirect:/";
            }
            if (StringUtils.isNotBlank(pageNumStr)) {
                //输入页码的是正整数就进行转换
                if (pageNumStr.matches("^[1-9]\\d*$")) {
                    pageNum = Integer.valueOf(pageNumStr);
                }
            }

            ViewObject pageVos = searchService.search(keyword, "-1", "-1", EntityType.ENTITY_QUESTION, pageNum, pageSize);

            if (pageVos==null){
                return "result";
            }
            List<Question> questionList = (List<Question>) pageVos.get("questionList");
            ViewObject pageVo = (ViewObject) pageVos.get("pageVo");

            List<ViewObject> vos = new ArrayList<>();
            for (Question question : questionList) {
                ViewObject vo = new ViewObject();
                vo.set("question", question);
                vo.set("user", userService.getUserById(question.getUserId()));
                vos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("keyword", keyword);
            model.addAttribute("pageVo", pageVo);
        } catch (Exception e) {
            logger.error("搜索问题失败" + e.getMessage());
        }
        return "result";
    }


    @RequestMapping(path = {"/search/questions"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String searchQuestion(Model model, @RequestParam(value = "q", defaultValue = "-1") String keyword,
                                 @RequestParam(value = "page", defaultValue = "1") String pageNumStr,
                                 @RequestParam(value = "qid", defaultValue = "-1") String id,
                                 @RequestParam(value = "status", defaultValue = "-1") String status) {
        try {
            Integer pageNum = 1;
            Integer pageSize = 50;

            if (StringUtils.isNotBlank(pageNumStr)) {
                //输入页码的是正整数就进行转换
                if (pageNumStr.matches("^[1-9]\\d*$")) {
                    pageNum = Integer.valueOf(pageNumStr);
                }
            }

            ViewObject pageVos = searchService.search(keyword, status, id, EntityType.ENTITY_QUESTION, pageNum, pageSize);

            if (pageVos==null){
                return "admin/listQuestion";
            }
            List<Question> questionList = (List<Question>) pageVos.get("questionList");
            ViewObject pageVo = (ViewObject) pageVos.get("pageVo");

            List<ViewObject> vos = new ArrayList<>();
            for (Question question : questionList) {
                ViewObject vo = new ViewObject();
                vo.set("question", question);
                vos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("keyword", keyword);
            model.addAttribute("qid", id);
            model.addAttribute("status", status);
            model.addAttribute("pageVo", pageVo);
        } catch (Exception e) {
            logger.error("搜索问题失败" + e.getMessage());
            throw new RuntimeException("搜索问题失败");
        }
        return "admin/listQuestion";
    }


    @RequestMapping(path = {"/search/comments"}, method = {RequestMethod.GET})
    public String searchComment(Model model, @RequestParam(value = "q", defaultValue = "-1") String keyword,
                                 @RequestParam(value = "page", defaultValue = "1") String pageNumStr,
                                 @RequestParam(value = "cid", defaultValue = "-1") String id,
                                 @RequestParam(value = "status", defaultValue = "-1") String status) {
        try {
            Integer pageNum = 1;
            Integer pageSize = 50;

            if (StringUtils.isNotBlank(pageNumStr)) {
                //输入页码的是正整数就进行转换
                if (pageNumStr.matches("^[1-9]\\d*$")) {
                    pageNum = Integer.valueOf(pageNumStr);
                }
            }

            ViewObject pageVos = searchService.search(keyword, status, id, EntityType.ENTITY_COMMENT, pageNum, pageSize);
            if (pageVos==null){
                return "admin/listComment";
            }
            List<Comment> commentList = (List<Comment>) pageVos.get("commentList");
            ViewObject pageVo = (ViewObject) pageVos.get("pageVo");

            List<ViewObject> vos = new ArrayList<>();
            for (Comment comment : commentList) {
                ViewObject vo = new ViewObject();
                vo.set("comment", comment);
                vos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("keyword", keyword);
            model.addAttribute("cid", id);
            model.addAttribute("status", status);
            model.addAttribute("pageVo", pageVo);
        } catch (Exception e) {
            logger.error("搜索评论失败" + e.getMessage());
            throw new RuntimeException("搜索评论失败");
        }
        return "admin/listComment";
    }


}
