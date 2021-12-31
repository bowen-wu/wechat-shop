package com.bowen.shop.service;

import com.bowen.shop.entity.Tel;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelVerificationService {
    private static final Pattern TEL_PATTERN = Pattern.compile("1\\d{10}");

    /**
     * 验证输入的参数是否合法: tel 必须存在且为合法的大陆手机号
     *
     * @param tel 输入的参数
     * @return true 合法 false 不合法
     */
    public boolean verifyTelParameter(Tel tel) {
        if (tel == null || tel.getTel() == null) {
            return false;
        }
        return TEL_PATTERN.matcher(tel.getTel()).find();
    }
}
