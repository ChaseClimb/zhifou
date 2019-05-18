package com.wenda.interceptor;

import com.wenda.dao.LoginTicketDao;
import com.wenda.dao.MessageDao;
import com.wenda.dao.UserDao;
import com.wenda.model.HostHolder;
import com.wenda.model.LoginTicket;
import com.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    LoginTicketDao loginTicketDao;

    @Autowired
    UserDao userDao;

    @Autowired
    MessageDao messageDao;

    //ThreadLocal
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals("ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }

        //检票
        if (ticket != null) {
            LoginTicket loginTicket = loginTicketDao.selectIdByTicket(ticket);
            //过期或状态不为0
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
                return true;
            }
            User user = userDao.getUserById(loginTicket.getUserId());
            hostHolder.setUsers(user);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //处理完成，渲染之前
        if (modelAndView != null) {
            User user = hostHolder.getUser();
            modelAndView.addObject("user", user);
            modelAndView.addObject("unReadMessageCount", messageDao.getUnreadMessageCount(user.getId()));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();
    }
}
