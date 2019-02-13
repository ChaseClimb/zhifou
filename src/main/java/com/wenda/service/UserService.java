package com.wenda.service;

import com.wenda.dao.LoginTicketDao;
import com.wenda.dao.UserDao;
import com.wenda.model.LoginTicket;
import com.wenda.model.User;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    @Autowired
    UserDao userDao;

    @Autowired
    LoginTicketDao loginTicketDao;

    public User getUserById(int id){
        return userDao.getUserById(id);
    }

    public Map<String, Object> login(String username, String password, String vCode, String redisCode) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(vCode)){
            map.put("msg","验证码不为空");
            return map;
        }
        if(redisCode==null){
            map.put("msg","验证码已过期，请重新获取");
            return map;
        }
        if (!vCode.equalsIgnoreCase(redisCode)){
            map.put("msg","验证码有误");
            return map;
        }

        User user = userDao.selectByName(username);
        if (user==null){
            map.put("msg","用户名不存在");
            return map;
        }
        if (!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","用户名或密码错误");
            return map;
        }
        //存入ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }



    public String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date date = new Date();
        date.setTime(3600*24*1000+date.getTime());
        loginTicket.setExpired(date);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replace("-",""));
        loginTicketDao.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    public Map<String, Object> register(String username, String password, String vCode, String redisCode) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(vCode)){
            map.put("msg","验证码不为空");
            return map;
        }
        if(redisCode==null){
            map.put("msg","验证码已过期，请重新获取");
            return map;
        }
        if (!vCode.equalsIgnoreCase(redisCode)){
            map.put("msg","验证码有误");
            return map;
        }

        User user = userDao.selectByName(username);
        if (user!=null){
            map.put("msg","用户名已存在");
            return map;
        }

        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDao.addUser(user);

        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);

        return map;
    }

    public void logout(String ticket) {
        loginTicketDao.updateStatus(ticket,1);

    }
}
