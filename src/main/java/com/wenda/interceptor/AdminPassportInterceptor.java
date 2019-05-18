package com.wenda.interceptor;

import com.wenda.dao.AdminDao;
import com.wenda.dao.AdminLoginTicketDao;
import com.wenda.dao.LoginTicketDao;
import com.wenda.dao.UserDao;
import com.wenda.model.HostHolder;
import com.wenda.model.LoginTicket;
import com.wenda.model.User;
import com.wenda.model.admin.Admin;
import com.wenda.model.admin.AdminHostHolder;
import com.wenda.model.admin.AdminLoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class AdminPassportInterceptor implements HandlerInterceptor {
    @Autowired
    AdminLoginTicketDao adminLoginTicketDao;

    @Autowired
    AdminDao adminDao;

    //ThreadLocal
    @Autowired
    AdminHostHolder adminHostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies()!=null){
            for (Cookie cookie:httpServletRequest.getCookies()){
                if (cookie.getName().equals("adminTicket")){
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        //检票
        if (ticket!=null){
            AdminLoginTicket adminLoginTicket = adminLoginTicketDao.selectIdByTicket(ticket);
            //过期或状态不为0
            if (adminLoginTicket==null||adminLoginTicket.getExpired().before(new Date())||adminLoginTicket.getStatus()!=0){
                return true;
            }
            Admin admin = adminDao.getUserById(adminLoginTicket.getUserId());
            adminHostHolder.setUsers(admin);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //处理完成，渲染之前
        if (modelAndView!=null){
            modelAndView.addObject("admin",adminHostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        adminHostHolder.clear();
    }
}
