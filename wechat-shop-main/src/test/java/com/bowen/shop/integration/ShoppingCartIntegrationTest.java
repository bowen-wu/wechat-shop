package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.entity.AddToShoppingCartGoods;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.Response;
import com.bowen.shop.entity.ResponseWithPages;
import com.bowen.shop.entity.ShoppingCartData;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class ShoppingCartIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    Environment environment;

    @Test
    public void testGetGoodsWithPageFromShoppingCartSuccess() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            ClassicHttpRequest getShoppingCartListRequest = createRequestBuilder(Method.GET, "/api/v1/shoppingCart?pageNum=2&pageSize=2", null);
            try (CloseableHttpResponse response = httpclient.execute(getShoppingCartListRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<ShoppingCartData>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                        new TypeReference<ResponseWithPages<List<ShoppingCartData>>>() {
                        });

                assertEquals(2, res.getPageNum());
                assertEquals(2, res.getPageSize());
                assertEquals(2, res.getTotalPage());
                assertEquals(1, res.getData().size());
                assertEquals(3, res.getData().get(0).getShop().getId());
                assertEquals(1, res.getData().get(0).getGoodsWithNumberList().size());
                assertEquals(5, res.getData().get(0).getGoodsWithNumberList().get(0).getId());
                assertEquals(1, res.getData().get(0).getGoodsWithNumberList().get(0).getNumber());
                assertEquals(3, res.getData().get(0).getGoodsWithNumberList().get(0).getShopId());
            }
        }
    }

    @Test
    public void returnNotFoundWhenDeleteShoppingCart() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            assertHttpException(httpclient, Method.DELETE, "/api/v1/shoppingCart/99", null, HTTP_NOT_FOUND, "商品未找到！goodsId：99");
        }
    }

    @Test
    public void testDeleteShoppingCart() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            ClassicHttpRequest deleteShoppingCartRequest = createRequestBuilder(Method.DELETE, "/api/v1/shoppingCart/1", null);
            try (CloseableHttpResponse response = httpclient.execute(deleteShoppingCartRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<ShoppingCartData> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                        new TypeReference<Response<ShoppingCartData>>() {
                        });

                assertEquals(1, res.getData().getShop().getId());
                assertEquals(1, res.getData().getGoodsWithNumberList().size());
                assertEquals(2, res.getData().getGoodsWithNumberList().get(0).getId());
                assertEquals(5, res.getData().getGoodsWithNumberList().get(0).getNumber());
                assertEquals(1, res.getData().getGoodsWithNumberList().get(0).getShopId());
            }

        }
    }

    @Test
    public void returnBadRequestWhenAddGoodsToShoppingCart() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            List<AddToShoppingCartGoods> goodsListPendingToShoppingCart = Collections.singletonList(new AddToShoppingCartGoods(0, 1));
            assertHttpException(httpclient, Method.POST, "/api/v1/shoppingCart", goodsListPendingToShoppingCart, HTTP_BAD_REQUEST, "请求参数错误！");
        }
    }

    @Test
    public void returnNotFoundWhenAddGoodsToShoppingCart() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            List<AddToShoppingCartGoods> goodsListPendingToShoppingCart = Collections.singletonList(new AddToShoppingCartGoods(1, 7));
            assertHttpException(httpclient, Method.POST, "/api/v1/shoppingCart", goodsListPendingToShoppingCart, HTTP_NOT_FOUND, "店铺未找到！shopId：5");

            assertHttpException(
                    httpclient,
                    Method.POST,
                    "/api/v1/shoppingCart",
                    Collections.singletonList(new AddToShoppingCartGoods(1, 999)),
                    HTTP_NOT_FOUND,
                    "商品未找到！goodsId：999");
        }
    }

    @Test
    public void testAddGoodsToShoppingCart() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            List<AddToShoppingCartGoods> goodsListPendingToShoppingCart = Collections.singletonList(new AddToShoppingCartGoods(1, 1));

            ClassicHttpRequest addShoppingCartRequest = createRequestBuilder(Method.POST, "/api/v1/shoppingCart", goodsListPendingToShoppingCart);
            try (CloseableHttpResponse response = httpclient.execute(addShoppingCartRequest)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<ShoppingCartData> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                        new TypeReference<Response<ShoppingCartData>>() {
                        });

                assertEquals(1, res.getData().getShop().getId());
                assertEquals(2, res.getData().getGoodsWithNumberList().size());
                assertEquals(1, res.getData().getShop().getId());
                assertEquals(Arrays.asList(1L, 2L), res.getData().getGoodsWithNumberList().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
                assertEquals(Arrays.asList(9, 5), res.getData().getGoodsWithNumberList().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
            }
        }
    }


}
