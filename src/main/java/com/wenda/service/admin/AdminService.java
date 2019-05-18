package com.wenda.service.admin;

import com.wenda.dao.AdminDao;
import com.wenda.dao.AdminLoginTicketDao;
import com.wenda.dao.LoginTicketDao;
import com.wenda.dao.UserDao;
import com.wenda.model.LoginTicket;
import com.wenda.model.User;
import com.wenda.model.admin.Admin;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {
    @Autowired
    AdminDao adminDao;

    @Autowired
    AdminLoginTicketDao adminLoginTicketDao;

    public Admin getUserById(int id){
        return adminDao.getUserById(id);
    }

    public Map<String, Object> login(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        Admin admin = adminDao.selectByName(username);
        if (admin==null){
            map.put("msg","用户名不存在");
            return map;
        }
        if (!admin.getPassword().equals(password)){
            map.put("msg","用户名或密码错误");
            return map;
        }
        //存入ticket
        String ticket = addLoginTicket(admin.getId());
        map.put("adminTicket",ticket);
        return map;
    }



    private String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date date = new Date();
        date.setTime(3600*24*1000+date.getTime());
        loginTicket.setExpired(date);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replace("-",""));
        adminLoginTicketDao.addTicket(loginTicket);
        return loginTicket.getTicket();
    }


    public void logout(String ticket) {
        adminLoginTicketDao.updateStatus(ticket,1);
    }
}
