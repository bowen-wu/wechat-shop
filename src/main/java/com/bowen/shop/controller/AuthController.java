package com.bowen.shop.controller;

import com.bowen.shop.entity.LoginResponse;
import com.bowen.shop.entity.Tel;
import com.bowen.shop.entity.TelAndCode;
import com.bowen.shop.generate.User;
import com.bowen.shop.service.AuthService;
import com.bowen.shop.service.TelVerificationService;
import com.bowen.shop.service.UserContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;
    private final TelVerificationService telVerificationService;

    @Autowired
    public AuthController(AuthService authService, TelVerificationService telVerificationService) {
        this.authService = authService;
        this.telVerificationService = telVerificationService;
    }

    @PostMapping("/code")
    public void code(@RequestBody Tel tel, HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(tel)) {
            authService.sendVerificationCode(tel.getTel());
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody TelAndCode telAndCode, HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(telAndCode)) {
            UsernamePasswordToken token = new UsernamePasswordToken(
                    telAndCode.getTel(),
                    telAndCode.getCode());
            token.setRememberMe(true);

            try {
                SecurityUtils.getSubject().login(token);
                response.setStatus(HttpStatus.FOUND.value());
            } catch (IncorrectCredentialsException e) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }

    @PostMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @GetMapping("/status")
    public LoginResponse getLoginStatus() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return LoginResponse.notLogin();
        }
        return LoginResponse.alreadyLogin(currentUser);
    }
}
