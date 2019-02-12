package com.wenda.model;

import org.springframework.stereotype.Component;

@Component
public class HostHolder {

    private static ThreadLocal<User> users = new ThreadLocal<>();
    private static ThreadLocal<String> tempIds = new ThreadLocal<>();

    public User getUser(){
        return users.get();
    }
    public void setUsers(User user){
        users.set(user);
    }
    public void clear(){
        users.remove();
    }

    public String getTempId(){
        return tempIds.get();
    }
    public void setTempId(String tempId){
        tempIds.set(tempId);
    }
    public void clearId(){
        tempIds.remove();
    }



}
