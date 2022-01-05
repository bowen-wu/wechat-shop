package com.bowen.shop.service;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class UserLoginInterceptor implements HandlerInterceptor {
    private final UserService userService;
    private final List<String> whiteList = new ArrayList<>();

    @Autowired
    public UserLoginInterceptor(UserService userService) {
        this.userService = userService;
        whiteList.add("/api/v1/code");
        whiteList.add("/api/v1/login");
        whiteList.add("/api/v1/status");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object tel = SecurityUtils.getSubject().getPrincipal();
        if (tel != null || whiteList.contains(request.getRequestURI())) {
            userService.getUserInfoByTel((String) tel).ifPresent(UserContext::setCurrentUser);
            return true;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        // 非常非常重要，线程会被复用
        UserContext.setCurrentUser(null);
    }
}
