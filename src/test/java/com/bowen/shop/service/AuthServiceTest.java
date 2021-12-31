package com.bowen.shop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UserService userService;
    @Mock
    VerificationCodeCheckService verificationCodeCheckService;
    @Mock
    SmsCodeService smsCodeService;
    @InjectMocks
    AuthService authService;

    @Test
    public void testSendVerificationCode() {
        String tel = "13700000000";
        String code = "code";
        Mockito.when(smsCodeService.sendSmsCode(tel)).thenReturn(code);
        authService.sendVerificationCode(tel);
        Mockito.verify(userService).createUserIfNotExist(tel);
        Mockito.verify(smsCodeService).sendSmsCode(tel);
        Mockito.verify(verificationCodeCheckService).addCode(tel, code);
    }
}
