package com.bowen.shop.service;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginInterceptor implements HandlerInterceptor {
    private final UserService userService;

    @Autowired
    public UserLoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * TODO: 如果没有登录直接返回 401，登录了之后可以继续，还需要添加白名单，如登录接口不需要判断是否登录
         */
        Object tel = SecurityUtils.getSubject().getPrincipal();
        if (tel != null) {
            userService.getUserInfoByTel((String) tel).ifPresent(UserContext::setCurrentUser);
        }
        return true;
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        // 非常非常重要，线程会被复用
        UserContext.setCurrentUser(null);
    }
}
