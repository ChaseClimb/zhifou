package com.wenda.service;

import com.wenda.dao.MessageDao;
import com.wenda.model.Message;
import com.wenda.util.JsoupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    MessageDao messageDao;

    @Autowired
    SensitiveService sensitiveService;


    public int addMessage(Message message) {
        String result = JsoupUtil.noneClean(message.getContent());
        message.setContent(sensitiveService.filter(result));
        return messageDao.addMessage(message);
    }

    //获取消息列表
    public List<Message> getConversationList(int userId) {
        return messageDao.getConversationList(userId);
    }

    public int getConvesationUnreadCount(int localUserId, String conversationId) {
        return messageDao.getConvesationUnreadCount(localUserId,conversationId);
    }

    public int readMessage(int localUserId, String conversationId) {
        return messageDao.readMessage(localUserId,conversationId);
    }

    public List<Message> getConversationDetail(String conversationId) {
        return messageDao.getConversationDetail(conversationId);
    }
}
