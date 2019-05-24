package com.wenda.async.handler;

import com.wenda.async.EventHandler;
import com.wenda.async.EventModel;
import com.wenda.async.EventType;
import com.wenda.model.EntityType;
import com.wenda.model.HostHolder;
import com.wenda.model.Message;
import com.wenda.model.User;
import com.wenda.service.MessageService;
import com.wenda.service.UserService;
import com.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUserById(model.getActorId());
        if (model.getEntityType() == EntityType.ENTITY_COMMENT) {
            //41     1
            if (model.getEntityOwnerId() != model.getActorId()) {
                message.setContent("用户 " + user.getName() + " 赞了你的评论,localhost:8080/question/" + model.getEntityId());
                messageService.addMessage(message);
            }
        } else {
            if (model.getEntityOwnerId() != model.getActorId()) {
                message.setContent("用户 " + user.getName() + " 赞了你的问题,localhost:8080/question/" + model.getEntityId());
                messageService.addMessage(message);
            }
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE_COMMENT, EventType.LIKE_QUESTION);
    }
}
