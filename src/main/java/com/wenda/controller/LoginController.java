package com.wenda.controller;

import com.wenda.model.HostHolder;
import com.wenda.model.User;
import com.wenda.service.UserService;
import com.wenda.util.JedisAdapter;
import com.wenda.util.VerifyCode;
import com.wenda.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping("/reg")
    public String reg() {
        return "register";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping(path = {"/regForm"}, method = {RequestMethod.POST})
    public String regForm(Model model, String username, String password, HttpServletResponse response,
                          @RequestParam(value = "next", required = false) String next, String vCode, @CookieValue(value = "tempId") String tempId) {
        Jedis jedis = null;
        try {
            jedis = jedisAdapter.getJedis();
            Map<String, Object> map = userService.register(username, password, vCode, jedis.get(tempId));
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                cookie.setMaxAge(3600 * 24);
                response.addCookie(cookie);
                //不为空
                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "register";
            }
        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            return "register";
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @RequestMapping(path = {"/loginForm"}, method = {RequestMethod.POST})
    public String loginForm(Model model, String username, String password,
                            HttpServletResponse response, @RequestParam(value = "next", required = false) String next, String vCode, @CookieValue(value = "tempId") String tempId) {
        Jedis jedis = null;
        try {
            jedis = jedisAdapter.getJedis();
            Map<String, Object> map = userService.login(username, password, vCode, jedis.get(tempId));
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                cookie.setMaxAge(3600 * 24);
                //向客户端写入cookie
                response.addCookie(cookie);

                //传入的next不为空时
                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        } catch (Exception e) {
            logger.error("登录异常" + e.getMessage());
            return "login";
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    @RequestMapping(path = {"/user/update"}, method = {RequestMethod.POST})
    @ResponseBody
    public String update(String password, String newPassword) {
        User holderUser = hostHolder.getUser();
        if (holderUser == null) {
            return WendaUtil.getJSONString(999);
        }
        String salt = holderUser.getSalt();
        if (StringUtils.isBlank(password)) {
            return WendaUtil.getJSONString(1, "密码不为空");
        }
        if (StringUtils.isBlank(newPassword)) {
            return WendaUtil.getJSONString(1, "新密码不为空");
        }
        if (!holderUser.getPassword().equals(WendaUtil.MD5(password + salt))) {
            return WendaUtil.getJSONString(1, "原密码错误");
        }
        User user = new User();
        user.setId(holderUser.getId());
        user.setPassword(WendaUtil.MD5(newPassword + salt));
        int rowCount = userService.update(user);
        if (rowCount > 0) {
            return WendaUtil.getJSONString(0);
        } /*else {
            return WendaUtil.getJSONString(1, "修改密码失败");
        }*/
        return "hhhh";
    }


    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET})
    public String logout(@CookieValue("ticket") String ticket, HttpServletResponse response) {
        userService.logout(ticket);
        Cookie newCookie = new Cookie("ticket", null);
        newCookie.setMaxAge(0);
        newCookie.setPath("/");
        response.addCookie(newCookie); //重新写入，将覆盖之前的
        return "redirect:/";
    }


    @RequestMapping("/genimage")
    public void genimage(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tempId = null;
        Jedis jedis = null;
        VerifyCode vc = new VerifyCode();
        //获取一次性验证码图片
        BufferedImage image = vc.getImage();
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        try {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("tempId")) {
                        if (StringUtils.isNotBlank(cookie.getValue())) {
                            tempId = cookie.getValue();
                        }
                        break;
                    }
                }
            }

            if (StringUtils.isBlank(tempId)) {
                //生成存放验证码的key，并存入cookie
                tempId = UUID.randomUUID().toString().replace("-", "")
                        + System.currentTimeMillis();
                Cookie cookie = new Cookie("tempId", tempId);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            jedis = jedisAdapter.getJedis();
            //验证码存入redis中，设置过期时间为3 min
            jedis.setex(tempId, 3 * 60, vc.getText());

            //把图片写到指定流中
            VerifyCode.output(image, response.getOutputStream());
        } catch (Exception e) {
            logger.error("获取验证码异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
