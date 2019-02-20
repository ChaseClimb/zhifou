package com.wenda.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wenda.model.*;
import com.wenda.service.FeedService;
import com.wenda.service.FollowService;
import com.wenda.service.UserService;
import com.wenda.util.JedisAdapter;
import com.wenda.util.RedisKeyUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    UserService userService;

    //主动拉取
    @RequestMapping(path = {"/pullfeeds"})
    private String getPullFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 找到自己关注的所有人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        List<Feed> feeds = feedService.getUserFeeds(followees);
        model.addAttribute("feeds", feeds);
        return "feeds";
    }


    @RequestMapping(path = {"/user/activities"}, method = {RequestMethod.GET})
    public String activities(Model model,  @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        Integer pageNum = 1;
        Integer pageSize = 20;

        if (StringUtils.isNotBlank(pageNumStr)) {
            //输入页码的是正整数就进行转换
            if (pageNumStr.matches("^[1-9]\\d*$")) {
                pageNum = Integer.valueOf(pageNumStr);
            }
        }

        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 找到自己关注的所有人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        else {
            return "redirect:/login";
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Feed> feeds = null;
        feeds = feedService.getUserFeeds(followees);

        PageInfo<Feed> page = new PageInfo<>(feeds);
        if (pageNum > page.getPages()) {
            pageNum = page.getPages();
            PageHelper.startPage(pageNum, pageSize);
            feeds = feedService.getUserFeeds(followees);
            page = new PageInfo<>(feeds);
        }

        List<ViewObject> feedVos = new ArrayList<>();
        for(Feed feed:feeds){
            User user = userService.getUserById(feed.getUserId());
            ViewObject vo = new ViewObject();
            vo.set("feed",feed);
            vo.set("userName",user.getName());
            feedVos.add(vo);
        }
        model.addAttribute("feeds", feedVos);

        ViewObject pageVo = new ViewObject();
        pageVo.set("pageNumber", page.getPageNum());
        pageVo.set("totalPage", page.getPages());

        model.addAttribute("pageVo", pageVo);

        return "activity";
    }

}
