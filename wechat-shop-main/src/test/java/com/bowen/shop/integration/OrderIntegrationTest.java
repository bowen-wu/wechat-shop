package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.entity.GoodsWithNumber;
import com.bowen.shop.entity.OrderResponse;
import com.bowen.shop.entity.Response;
import com.bowen.shop.generate.User;
import com.bowen.shop.mock.MockOrderRpcService;
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
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_GONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    Environment environment;

    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(mockOrderRpcService);

        when(mockOrderRpcService.getOrderRpcService().createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Order order = invocation.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });
    }

    @Test
    public void canCreateOrder() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            User loggedUser = login(httpclient);

            List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(6, 4));
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(7, 5));

            ClassicHttpRequest createGoods = createRequestBuilder(Method.POST, "/api/v1/order", testGoodsIdAndNumberList);
            try (CloseableHttpResponse response = httpclient.execute(createGoods)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<OrderResponse> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<OrderResponse>>() {
                });

                assertEquals(1234L, res.getData().getId());
                assertEquals(3, res.getData().getShop().getId());
                assertEquals(2, res.getData().getGoodsList().size());
                assertEquals(Arrays.asList(4L, 5L), res.getData().getGoodsList().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
                assertEquals(Arrays.asList(6, 7), res.getData().getGoodsList().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
                assertEquals(loggedUser.getId(), res.getData().getUserId());
                assertEquals(16500, res.getData().getTotalPrice());
                assertEquals(DataStatus.PENDING.getStatus(), res.getData().getStatus());
            }


        }
    }

    @Test
    public void canRollBackIfDeductStockFailed() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(11, 6));
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(5, 7));

            assertHttpException(httpclient, Method.POST, "/api/v1/order", testGoodsIdAndNumberList, HTTP_GONE, "扣减库存失败！");

            // check rollback, can deduct stock
            canCreateOrder();
        }
    }


}
