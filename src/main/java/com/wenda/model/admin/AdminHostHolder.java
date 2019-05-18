package com.wenda.model.admin;

import com.wenda.model.User;
import org.springframework.stereotype.Component;

@Component
public class AdminHostHolder {

    private static ThreadLocal<Admin> admins = new ThreadLocal<>();
    public Admin getUser(){
        return admins.get();
    }
    public void setUsers(Admin admin){
        admins.set(admin);
    }
    public void clear(){
        admins.remove();
    }

}
