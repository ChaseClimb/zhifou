package com.wenda.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.model.User;
import com.wenda.model.ViewObject;
import com.wenda.service.CommentService;
import com.wenda.service.QuestionService;
import com.wenda.service.UserService;
import com.wenda.util.JsoupUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;
        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数才进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }
        ViewObject vos = getQuestions(pageNum, pageSize);
        model.addAttribute("qVos", vos.get("qVos"));
        model.addAttribute("pageVo", vos.get("pageVo"));
        return "index";
    }

    private ViewObject getQuestions(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> questionList = null;
        questionList = questionService.selectAllQuestions();

        PageInfo<Question> page = new PageInfo<>(questionList);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            questionList = questionService.selectAllQuestions();
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
            vo.set("user", userService.getUserById(question.getUserId()));
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


    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET})
    public String userIndex(Model model, @PathVariable("userId") Integer userId, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;

        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数就进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }

        ViewObject vos = getQuestions(userId,pageNum, pageSize);
        model.addAttribute("qVos", vos.get("qVos"));
        model.addAttribute("pageVo", vos.get("pageVo"));

        User user = userService.getUserById(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("questionCount",questionService.getUserQuestionCount(userId));

        model.addAttribute("userInfo", vo);
        return "user";
    }



}
