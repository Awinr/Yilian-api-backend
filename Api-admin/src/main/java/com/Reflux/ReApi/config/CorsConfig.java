package com.Reflux.ReApi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 * @author Aaron
 */
// 取消注释开启Interceptor里的跨域处理，
// 但这个跨域处理会和自定义拦截器冲突，跨域拦截器只会在自定义拦截器之后执行，
// 并且拦截器是链式，一旦一个不放行，后面都不会执行，
// 所以跨域拦截器只能在自定义不拦截的路径上生效，比如login
//@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                // 放行所有域名...
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //表示访问请求中允许携带哪些Header信息，如：Accept、Accept-Language、Content-Language、Content-Type
                .allowedHeaders("*")
                //暴露哪些头部信息(因为跨域访问默认不能获取全部头部信息)
                .exposedHeaders("*");
    }
}
