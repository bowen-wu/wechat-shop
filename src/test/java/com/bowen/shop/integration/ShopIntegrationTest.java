package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.entity.DataStatus;
import com.bowen.shop.entity.Response;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.generate.Shop;
import com.bowen.shop.generate.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
public class ShopIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    Environment environment;

    private static Shop testShop = null;

    @BeforeEach
    void beforeEach() {
        testShop = new Shop();
        testShop.setName("My shop!");
        testShop.setDescription("Description of my shop!");
        testShop.setImgUrl("http://shop.url");
        testShop.setStatus(DataStatus.OK.getStatus());
    }

    @AfterEach
    void afterEach() {
        testShop = null;
    }

    @Test
    public void testShopLifeCycle() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            User loggedInUser = login(httpclient);
            Long createdShopId;

            // 1. create shop => createdShopId
            ClassicHttpRequest createShopRequest = createRequestBuilder(Method.POST, "/api/v1/shop", testShop);
            try (CloseableHttpResponse response = httpclient.execute(createShopRequest)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<Shop> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Shop>>() {
                });
                matchAllAttribute(res, loggedInUser);
                createdShopId = res.getData().getId();
                testShop.setId(res.getData().getId());
            }

            // 2. get shop via createdShopId
            ClassicHttpRequest getShopInfoByIdRequest = createRequestBuilder(Method.GET, "/api/v1/shop/" + createdShopId, null);
            try (CloseableHttpResponse response = httpclient.execute(getShopInfoByIdRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<Shop> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Shop>>() {
                });
                matchAllAttribute(res, loggedInUser);
                assertEquals(createdShopId, res.getData().getId());
            }

            // 3. update shop via createdShopId
            testShop.setName("Update shop name");
            testShop.setImgUrl("http://update.url");
            testShop.setDescription("Update description");
            ClassicHttpRequest updateShopRequest = createRequestBuilder(Method.PATCH, "/api/v1/shop", testShop);
            try (CloseableHttpResponse response = httpclient.execute(updateShopRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<Shop> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Shop>>() {
                });
                matchAllAttribute(res, loggedInUser);
                assertEquals(createdShopId, res.getData().getId());
            }

            // 4. get shop info via createdShopId
            try (CloseableHttpResponse response = httpclient.execute(getShopInfoByIdRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<Shop> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<Shop>>() {
                });
                matchAllAttribute(res, loggedInUser);
                assertEquals(createdShopId, res.getData().getId());
            }

            // 5. get shopListWithPage
            int pageNum = 2;
            int pageSize = 2;
            ClassicHttpRequest getListWithPageRequest = createRequestBuilder(Method.GET, "/api/v1/shop?pageNum=" + pageNum + "&pageSize=" + pageSize, null);
            try (CloseableHttpResponse response = httpclient.execute(getListWithPageRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<Shop>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<Shop>>>() {
                });
                assertEquals(pageNum, res.getPageNum());
                assertEquals(pageSize, res.getPageSize());
                assertEquals(2, res.getTotalPage());
                assertEquals(1, res.getData().size());
            }

            // 6. delete shop => createdShopId
            ClassicHttpRequest deleteShopRequest = createRequestBuilder(Method.DELETE, "/api/v1/shop/" + createdShopId, null);
            try (CloseableHttpResponse response = httpclient.execute(deleteShopRequest)) {
                assertEquals(HTTP_NO_CONTENT, response.getCode());
            }

            // 7. get shop info => Not Found
            try (CloseableHttpResponse response = httpclient.execute(getShopInfoByIdRequest)) {
                assertEquals(HTTP_NOT_FOUND, response.getCode());
            }
        }
    }

    private void matchAllAttribute(Response<Shop> res, User loggedInUser) {
        assertEquals(testShop.getStatus(), res.getData().getStatus());
        assertEquals(testShop.getDescription(), res.getData().getDescription());
        assertEquals(testShop.getName(), res.getData().getName());
        assertEquals(testShop.getImgUrl(), res.getData().getImgUrl());
        assertEquals(loggedInUser.getId(), res.getData().getOwnerUserId());
    }

    @Test
    public void returnBadRequestWhenCreateShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            Shop badRequestShop = new Shop();

            assertHttpException(httpclient, Method.POST, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestShop.setName("name");
            assertHttpException(httpclient, Method.POST, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestShop.setDescription("description");
            assertHttpException(httpclient, Method.POST, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
        }
    }

    @Test
    public void returnNotFoundWhenDeleteShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            assertHttpException(httpclient, Method.DELETE, "/api/v1/shop/999", null, HTTP_NOT_FOUND, "店铺不存在！");
        }
    }

    @Test
    public void returnForbiddenWhenDeleteShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            assertHttpException(httpclient, Method.DELETE, "/api/v1/shop/4", null, HTTP_FORBIDDEN, "不能删除非自己管理的店铺！");
        }
    }

    @Test
    public void returnBadRequestWhenUpdateShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            Shop badRequestShop = new Shop();

            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestShop.setId(99L);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestShop.setName("name");
            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
            badRequestShop.setDescription("description");
            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", badRequestShop, HTTP_BAD_REQUEST, "请检查参数！");
        }
    }

    @Test
    public void returnNotFoundWhenUpdateShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            testShop.setId(99L);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", testShop, HTTP_NOT_FOUND, "店铺不存在！");
        }
    }

    @Test
    public void returnForbiddenWhenUpdateShop() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            testShop.setId(4L);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/shop", testShop, HTTP_FORBIDDEN, "不能更新非自己管理的店铺！");
        }
    }

    @Test
    public void testGetShopListWithPage() throws Exception {

    }
}
