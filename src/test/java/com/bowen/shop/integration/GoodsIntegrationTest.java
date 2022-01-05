package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.Response;
import com.bowen.shop.generate.Goods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
class GoodsIntegrationTest {
    @Autowired
    Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Goods testGoods = new Goods();

    @BeforeEach
    void beforeAll() {
        testGoods.setShopId(1L);
        testGoods.setDescription("description");
        testGoods.setDetails("details");
        testGoods.setName("goods");
        testGoods.setPrice(new BigDecimal(100));
        testGoods.setImageUrl("http://url");
        testGoods.setStock(10);
    }

    @Test
    public void createGoodsSuccess() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            HttpRequest.login(httpclient, environment);
            final ClassicHttpRequest sendSmsCode = HttpRequest.createRequestBuilder(environment, Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<Goods> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Goods>>() {
                });
                assertEquals(testGoods.getShopId(), res.getData().getShopId());
                assertEquals(testGoods.getDescription(), res.getData().getDescription());
                assertEquals(testGoods.getDetails(), res.getData().getDetails());
                assertEquals(testGoods.getName(), res.getData().getName());
                assertEquals(testGoods.getPrice(), res.getData().getPrice());
                assertEquals(testGoods.getImageUrl(), res.getData().getImageUrl());
                assertEquals(testGoods.getStock(), res.getData().getStock());
                assertEquals(DataStatus.OK.getStatus(), res.getData().getStatus());
            }
        }
    }

    @Test
    public void returnErrorWhenCreateGoods() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            HttpRequest.login(httpclient, environment);
            ClassicHttpRequest sendSmsCode = HttpRequest.createRequestBuilder(environment, Method.POST, "/api/v1/goods", new Goods());
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_BAD_REQUEST, response.getCode());
            }
            testGoods.setShopId(10L);
            sendSmsCode = HttpRequest.createRequestBuilder(environment, Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_NOT_FOUND, response.getCode());
            }
            testGoods.setShopId(3L);
            sendSmsCode = HttpRequest.createRequestBuilder(environment, Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(sendSmsCode)) {
                assertEquals(HTTP_FORBIDDEN, response.getCode());
            }
        }
    }
}
