package com.Reflux.ReApi.config;

import com.Reflux.ReApi.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //拦截器对象
    @Resource
    private LoginCheckInterceptor loginCheckInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器对象，拦截所有路径，排除登录，不拦截登录
        registry.addInterceptor(loginCheckInterceptor)
                //.addPathPatterns("/**")//拦截所有请求，而且在放行以后还会被拦截回来
                //排除路径
                .excludePathPatterns(
                        //有注册，登录，和微信登录三个需要放行
                        "/user/login",
                        "/user/login/wx_open",
                        "/user//register",
                        //放行swagger
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources",
                        "/v3/api-docs",
                        "/v2/api-docs"
                );
    }
}
