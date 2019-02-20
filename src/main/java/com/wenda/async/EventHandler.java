package com.wenda.async;

import java.util.List;

//事件处理器接口
public interface EventHandler {
    //处理函数
    void doHandle(EventModel model);

    //得到事件处理器所支持的事件类型
    List<EventType> getSupportEventTypes();

}
