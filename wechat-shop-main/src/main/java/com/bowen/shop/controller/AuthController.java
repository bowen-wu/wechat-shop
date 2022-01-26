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

    // @formatter:off
    /**
     * @api {post} /code 请求验证码
     * @apiName GetCode
     * @apiGroup 登录与鉴权
     *
     * @apiParam {String} tel 手机号码
     * @apiParamExample {json} Request-Example:
     *          {
     *              "tel": "13800001234"
     *          }
     *
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     * @apiError 400 Bad Request 若用户请求包含错误
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 发送验证码
     *
     * @param tel      手机号
     * @param response response
     */
    // @formatter:on
    @PostMapping("/code")
    public void code(@RequestBody Tel tel, HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(tel)) {
            authService.sendVerificationCode(tel.getTel());
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * @api {post} /login 登录
     * @apiName Login
     * @apiGroup 登录与鉴权
     *
     * @apiParam {String} tel 手机号码
     * @apiParam {String} code 验证码
     * @apiParamExample {json} Request-Example:
     *      {
     *          "tel": "13700001234",
     *          "code": "000000"
     *      }
     *
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     * @apiError 400 Bad Request 若用户请求包含错误
     * @apiError 403 Forbidden 若用户的验证码错误
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 登录
     *
     * @param telAndCode 手机号和验证码
     * @param response   response
     */
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

    /**
     * @api {post} /logout 登出
     * @apiName Logout
     * @apiGroup 登录与鉴权
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *
     * @apiError 401 Unauthorized 若用户未登录
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 登出
     */
    @PostMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    /**
     * @api {get} /status 获取登录状态
     * @apiName Status
     * @apiGroup 登录与鉴权
     * @apiSuccessExample Success-Response:
     *      HTTP/1.1 200 OK
     *      {
     *          "login": true,
     *          "user": {
     *              "id": 123,
     *              "name": "张三",
     *              "tel": "13700001234",
     *              "avatarUrl": "http://url",
     *          }
     *      }
     *
     * @apiError 401 Unauthorized 若用户未登录
     *
     * @apiErrorExample Error-Response:
     *      HTTP/1.1 400 Bad Request
     *      {
     *          "message": "Bad Request"
     *      }
     */
    /**
     * 获取登录状态
     *
     * @return 登录
     */
    @GetMapping("/status")
    public LoginResponse getLoginStatus() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return LoginResponse.notLogin();
        }
        return LoginResponse.alreadyLogin(currentUser);
    }
}
