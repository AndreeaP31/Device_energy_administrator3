package com.example.demo;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class FilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleFilter roleFilter;

    public FilterConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RoleFilter roleFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.roleFilter = roleFilter;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter corsFilter) {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(corsFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }


    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.addInitParameter("excludeUrls", "/auth/**,/ws-message/**");
        registrationBean.setOrder(2); // after CORS
        return registrationBean;
    }


    @Bean
    public FilterRegistrationBean<RoleFilter> roleFilterBean() {
        FilterRegistrationBean<RoleFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(roleFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(3); // after JWT
        return registrationBean;
    }
}
