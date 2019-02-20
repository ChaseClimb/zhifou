package com.wenda.controller;

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
            ViewObject pageVos = searchService.searchQuestion(keyword, pageNum, pageSize);
            List<Question> questionList = (List<Question>) pageVos.get("questionList");
            ViewObject pageVo = (ViewObject) pageVos.get("pageVo");

            List<ViewObject> vos = new ArrayList<>();
            for (Question question : questionList) {
                Question q = questionService.getQuestionsById(question.getId());
                ViewObject vo = new ViewObject();
                if (question.getContent() != null) {
                    q.setContent(question.getContent());
                }
                if (question.getTitle() != null) {
                    q.setTitle(question.getTitle());
                }
                vo.set("question", q);
                vo.set("user", userService.getUserById(q.getUserId()));
                vos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("keyword", keyword);
            model.addAttribute("pageVo", pageVo);
        } catch (Exception e) {
            logger.error("搜索评论失败" + e.getMessage());
        }
        return "result";
    }
}
