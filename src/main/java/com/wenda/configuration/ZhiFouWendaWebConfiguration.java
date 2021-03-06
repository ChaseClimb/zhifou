package com.wenda.configuration;

import com.wenda.interceptor.AdminPassportInterceptor;
import com.wenda.interceptor.LoginRequiredInterceptor;
import com.wenda.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class ZhiFouWendaWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    AdminPassportInterceptor adminPassportInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor);
        registry.addInterceptor(adminPassportInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/question/add","/msg/*","/addComment");
        super.addInterceptors(registry);
    }
}
