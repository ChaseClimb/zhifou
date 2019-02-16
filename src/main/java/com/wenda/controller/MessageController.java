package com.wenda.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wenda.model.*;
import com.wenda.service.MessageService;
import com.wenda.service.UserService;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);


    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @RequestMapping(path = "/msg/add", method = RequestMethod.POST)
    @ResponseBody
    public String addMessage(@RequestParam("toId") Integer toId
            , @RequestParam("content") String content) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999, "未登录");
            }
            //找出收信方
            User user = userService.getUserById(toId);
            if (user == null) {
                return WendaUtil.getJSONString(1, "用户不存在");
            }
            Message message = new Message();
            message.setCreatedDate(new Date());
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setContent(content);
            //小id放前面、大id放后面
            message.setConversationId(message.getConversationId());
            int rowCount = messageService.addMessage(message);
            if (rowCount > 0) {
                return WendaUtil.getJSONString(0);
            } else {
                return WendaUtil.getJSONString(1, "发送私信失败");
            }
        } catch (Exception e) {
            logger.error("发送私信失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "发送私信失败");
        }
    }


    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversation(Model model, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            int localUserId = hostHolder.getUser().getId();

            Integer pageNum = 1;
            Integer pageSize = 5;
            if (StringUtils.isNotBlank(pageNumStr)) {
                //输入页码的是正整数才进行转换
                if (pageNumStr.matches("^[1-9]\\d*$")) {
                    pageNum = Integer.valueOf(pageNumStr);
                }
            }


            PageHelper.startPage(pageNum, pageSize);
            //消息列表
            List<Message> conversationList = messageService.getConversationList(localUserId);
            PageInfo<Message> page = new PageInfo<>(conversationList);

            if (pageNum > page.getPages()) {
                pageNum = page.getPages();
                PageHelper.startPage(pageNum, pageSize);
                conversationList = messageService.getConversationList(localUserId);
                page = new PageInfo<>(conversationList);
            }

            ViewObject pageVo = new ViewObject();
            pageVo.set("pageNumber", page.getPageNum());
            pageVo.set("totalPage", page.getPages());

            List<ViewObject> conversations = new ArrayList<ViewObject>();
            for (Message message : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                //判断用户是否是其中一方，并读取另外一方信息
                int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
                User user = userService.getUserById(targetId);
                User lastMessageOwner = userService.getUserById(message.getFromId());
                vo.set("user", user);
                //最新一条消息归属哪一用户
                vo.set("lmUser", lastMessageOwner);
                //接收方有几条未读
                vo.set("unread", messageService.getConvesationUnreadCount(localUserId, message.getConversationId()));
                conversations.add(vo);
            }

            model.addAttribute("conversations", conversations);
            model.addAttribute("pageVo", pageVo);
        } catch (Exception e) {
            logger.error("获取消息列表失败" + e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = "/msg/detail", method = RequestMethod.GET)
    public String conversationDetail(Model model, @RequestParam("conversationId") String conversationId, @RequestParam(value = "page", defaultValue = "1") String pageNumStr) {
        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999);
            }
            int localUserId = hostHolder.getUser().getId();
            String[] arr = conversationId.split("_");
            String targetId = arr[0].equals(String.valueOf(localUserId)) ? arr[1] : arr[0];
            User targetUser = userService.getUserById(Integer.parseInt(targetId));
            //更新阅读状态
            int rowCount = messageService.readMessage(localUserId, conversationId);
            if (rowCount == 0) {
                logger.error("更新阅读状态失败");
            }


            Integer pageNum = 1;
            Integer pageSize = 5;
            if (StringUtils.isNotBlank(pageNumStr)) {
                //输入页码的是正整数才进行转换
                if (pageNumStr.matches("^[1-9]\\d*$")) {
                    pageNum = Integer.valueOf(pageNumStr);
                }
            }

            PageHelper.startPage(pageNum, pageSize);
            List<Message> messageList = messageService.getConversationDetail(conversationId);
            PageInfo<Message> page = new PageInfo<>(messageList);

            if (pageNum > page.getPages()) {
                pageNum = page.getPages();
                PageHelper.startPage(pageNum, pageSize);
                messageList = messageService.getConversationDetail(conversationId);
                page = new PageInfo<>(messageList);
            }

            ViewObject pageVo = new ViewObject();
            pageVo.set("pageNumber", page.getPageNum());
            pageVo.set("totalPage", page.getPages());

            List<ViewObject> messages = new ArrayList<>();
            for (Message msg : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message", msg);
                User user = userService.getUserById(msg.getFromId());
                if (user == null) {
                    continue;
                }
                vo.set("user", user);
                messages.add(vo);
            }
            model.addAttribute("messages", messages);
            model.addAttribute("pageVo", pageVo);
            model.addAttribute("targetUser",targetUser);
        } catch (Exception e) {
            logger.error("获取详情消息失败" + e.getMessage());
        }
        return "letterDetail";
    }

}
