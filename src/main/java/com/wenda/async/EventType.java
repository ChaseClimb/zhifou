package com.wenda.async;

/**
 * 记录事件类型
 */
public enum EventType {
    //点赞
    LIKE_QUESTION(0),
    //点赞评论
    LIKE_COMMENT(1),
    //评论
    COMMENT(2),
    //关注
    FOLLOW(3),
    //取消关注
    UNFOLLOW(4),
    //添加问题后增加索引
    ADD_QUESTION(5);

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
