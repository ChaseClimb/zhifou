package com.wenda.controller.admin;

import com.wenda.service.UserService;
import com.wenda.service.admin.AdminService;
import com.wenda.util.JedisAdapter;
import com.wenda.util.VerifyCode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {
    private static final Logger logger = LoggerFactory.getLogger(AdminLoginController.class);

    @Autowired
    AdminService adminService;


    @RequestMapping("/login")
    public String login(){
        return "admin/login";
    }


    @RequestMapping(path = {"/loginForm"})
    public String loginForm(Model model, String username, String password,
                            HttpServletResponse response, @RequestParam(value = "next", required = false) String next) {
        try {
            Map<String, Object> map = adminService.login(username, password);
            if (map.containsKey("adminTicket")) {
                Cookie cookie = new Cookie("adminTicket", map.get("adminTicket").toString());
                cookie.setPath("/");
                cookie.setMaxAge(3600 * 24);
                //向客户端写入cookie
                response.addCookie(cookie);

                return "redirect:/admin/questions";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "admin/login";
            }
        } catch (Exception e) {
            logger.error("登录异常" + e.getMessage());
            return "admin/login";
        }

    }


    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET})
    public String logout(@CookieValue("adminTicket") String adminTicket,HttpServletResponse response) {
        adminService.logout(adminTicket);
        Cookie newCookie=new Cookie("adminTicket",null);
        newCookie.setMaxAge(0);
        newCookie.setPath("/");
        response.addCookie(newCookie); //重新写入，将覆盖之前的
        return "redirect:/admin/login";
    }




}
