package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class CodeIntegrationTest {
    @Autowired
    Environment environment;

    @Test
    public void returnHttpOKWhenParameterIsCorrect() {

    }

    @Test
    public void returnHttpBadRequestWhenParameterIsCorrect() {

    }
}
