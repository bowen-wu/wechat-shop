package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.Response;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Goods;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
class GoodsIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    Environment environment;

    private static final Goods testGoods = new Goods();

    @BeforeEach
    void beforeAll() {
        testGoods.setShopId(1L);
        testGoods.setDescription("description");
        testGoods.setDetails("details");
        testGoods.setName("goods");
        testGoods.setPrice(new BigDecimal(100));
        testGoods.setImageUrl("http://url");
        testGoods.setStatus(DataStatus.OK.getStatus());
    }

    @Test
    public void testGoodsLifeCycle() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            // 1. 增加商品 => 获取到 goodsId
            ClassicHttpRequest createGoods = createRequestBuilder(Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(createGoods)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<Goods> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Goods>>() {
                });
                matchAllAttribute(res);
                testGoods.setId(res.getData().getId());
            }

            // 2. 更改商品
            testGoods.setStock(1);
            testGoods.setName("New name");
            testGoods.setPrice(new BigDecimal(10));
            ClassicHttpRequest updateGoods = createRequestBuilder(Method.PATCH, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(updateGoods)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<Goods> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Goods>>() {
                });
                matchAllAttribute(res);
            }

            // 3. 查询该商品
            ClassicHttpRequest getGoodsById = createRequestBuilder(Method.GET, "/api/v1/goods/" + testGoods.getId(), null);
            try (CloseableHttpResponse response = httpclient.execute(getGoodsById)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<Goods> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Goods>>() {
                });
                matchAllAttribute(res);
            }

            // 4. 删除该商品
            ClassicHttpRequest deleteGoods = createRequestBuilder(Method.DELETE, "/api/v1/goods/" + testGoods.getId(), null);
            try (CloseableHttpResponse response = httpclient.execute(deleteGoods)) {
                assertEquals(HTTP_NO_CONTENT, response.getCode());
            }

            // 3. 查询该商品
            ClassicHttpRequest getGoodsByIdAfterDelete = createRequestBuilder(Method.GET, "/api/v1/goods/" + testGoods.getId(), null);
            try (CloseableHttpResponse response = httpclient.execute(getGoodsByIdAfterDelete)) {
                assertEquals(HTTP_NOT_FOUND, response.getCode());
            }
        }
    }

    public void matchAllAttribute(Response<Goods> res) {
        assertEquals(testGoods.getShopId(), res.getData().getShopId());
        assertEquals(testGoods.getDescription(), res.getData().getDescription());
        assertEquals(testGoods.getDetails(), res.getData().getDetails());
        assertEquals(testGoods.getName(), res.getData().getName());
        assertEquals(testGoods.getPrice(), res.getData().getPrice());
        assertEquals(testGoods.getImageUrl(), res.getData().getImageUrl());
        assertEquals(testGoods.getStock() == null ? 0 : testGoods.getStock(), res.getData().getStock());
        assertEquals(DataStatus.OK.getStatus(), res.getData().getStatus());
    }

    @Test
    public void returnExceptionWhenCreateGoods() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            Goods badRequestGoods = new Goods();

            assertHttpException(httpclient, Method.POST, "/api/v1/goods", badRequestGoods, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestGoods.setName("name");
            assertHttpException(httpclient, Method.POST, "/api/v1/goods", badRequestGoods, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestGoods.setPrice(new BigDecimal(100));
            assertHttpException(httpclient, Method.POST, "/api/v1/goods", badRequestGoods, HTTP_BAD_REQUEST, "请检查参数！");

            testGoods.setShopId(10L);
            assertHttpException(httpclient, Method.POST, "/api/v1/goods", testGoods, HTTP_NOT_FOUND, "店铺不存在！");

            testGoods.setShopId(3L);
            assertHttpException(httpclient, Method.POST, "/api/v1/goods", testGoods, HTTP_FORBIDDEN, "不能创建非自己店铺的商品！");
        }
    }


    @Test
    public void returnExceptionWhenDeleteGoods() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            assertHttpException(httpclient, Method.DELETE, "/api/v1/goods/999", null, HTTP_NOT_FOUND, "商品不存在！");

            // 4 号商品属于 3 号店铺，属于 2 号用户，当前登录的是 1 号用户
            assertHttpException(httpclient, Method.DELETE, "/api/v1/goods/4", null, HTTP_FORBIDDEN, "不能删除非自己店铺的商品！");
        }
    }

    @Test
    public void returnExceptionWhenGetGoodsById() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            assertHttpException(httpclient, Method.GET, "/api/v1/goods/", null, HTTP_BAD_REQUEST, "");
            assertHttpException(httpclient, Method.GET, "/api/v1/goods/99", null, HTTP_NOT_FOUND, "商品不存在！");
        }
    }

    @Test
    public void returnExceptionWhenUpdateGoods() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            testGoods.setId(99L);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/goods", testGoods, HTTP_NOT_FOUND, "商品不存在！");

            testGoods.setId(5L);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/goods", testGoods, HTTP_FORBIDDEN, "不能更新非自己店铺的商品！");
        }
    }

    @Test
    public void getGoodsListSuccess() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            ClassicHttpRequest createGoods = createRequestBuilder(Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(createGoods)) {
                assertEquals(HTTP_CREATED, response.getCode());
            }

            ClassicHttpRequest updateGoods = createRequestBuilder(Method.GET, "/api/v1/goods?pageNum=2&pageSize=4", null);
            try (CloseableHttpResponse response = httpclient.execute(updateGoods)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<Goods>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<Goods>>>() {
                });
                assertEquals(2, res.getPageNum());
                assertEquals(4, res.getPageSize());
                assertEquals(2, res.getTotalPage());
                assertEquals(4, res.getData().size());
            }
        }
    }

    @Test
    public void getGoodsListWithShopIdSuccess() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            ClassicHttpRequest createGoods = createRequestBuilder(Method.POST, "/api/v1/goods", testGoods);
            try (CloseableHttpResponse response = httpclient.execute(createGoods)) {
                assertEquals(HTTP_CREATED, response.getCode());
            }

            ClassicHttpRequest updateGoods = createRequestBuilder(Method.GET, "/api/v1/goods?pageNum=1&pageSize=2&shopId=1", null);
            try (CloseableHttpResponse response = httpclient.execute(updateGoods)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<Goods>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<Goods>>>() {
                });
                assertEquals(1, res.getPageNum());
                assertEquals(2, res.getPageSize());
                assertEquals(2, res.getTotalPage());
                assertEquals(2, res.getData().size());
            }
        }
    }
}
