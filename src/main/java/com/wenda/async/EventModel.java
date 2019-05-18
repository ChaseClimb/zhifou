package com.wenda.async;

import java.util.HashMap;
import java.util.Map;

//事件类型
public class EventModel {
    //事件类型（点赞）
    private EventType type;
    //事件发起人（点赞的人）
    private int actorId;
    //操作实体类型（给什么东西点赞）
    private int entityType;
    //操作实体类型的 ID（东西的ID）
    private int entityId;
    //实体类型的拥有者（东西的所属人）
    private int entityOwnerId;

    private Map<String, String> exts = new HashMap<>();
    public EventModel() {

    }

    public EventModel(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public String getExt(String key) {
        return exts.get(key);
    }

    public EventModel setExt(String key, String value) {
        exts.put(key, value);
        return this;
    }

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel setExts(Map<String, String> exts) {
        this.exts = exts;
        return this;
    }
}
