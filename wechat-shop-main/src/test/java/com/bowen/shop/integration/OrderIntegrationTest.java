package com.bowen.shop.integration;

import com.bowen.shop.WechatShopApplication;
import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
        MockitoAnnotations.openMocks(mockOrderRpcService);
    }

    @Test
    public void testOrderLifeCycle() throws Exception {
        long testOrderId = 1234L;
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            User loggedUser = login(httpclient);

            List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(6, 1));
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(7, 2));
            Order testOrder = new Order();

            // 创建一个订单
            when(mockOrderRpcService.getOrderRpcService().createOrder(any(), any())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(1);
                order.setId(testOrderId);
                order.setStatus(DataStatus.PENDING.getStatus());
                return order;
            });

            ClassicHttpRequest createOrder = createRequestBuilder(Method.POST, "/api/v1/order", testGoodsIdAndNumberList);
            try (CloseableHttpResponse response = httpclient.execute(createOrder)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<OrderResponse> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<OrderResponse>>() {
                });

                matchAllProperty(res.getData());
                assertEquals(loggedUser.getId(), res.getData().getUserId());
                assertEquals(DataStatus.PENDING.getStatus(), res.getData().getStatus());

                testOrder.setId(res.getData().getId());
                testOrder.setShopId(res.getData().getShop().getId());
            }

            // 更新订单 => 状态
            when(mockOrderRpcService.getOrderRpcService().updateOrder(any())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                RpcOrder rpcOrder = new RpcOrder(order);
                rpcOrder.setId(testOrderId);
                rpcOrder.setGoodsIdAndNumberList(testGoodsIdAndNumberList);
                rpcOrder.setTotalPrice(16500L);
                rpcOrder.setExpressCompany("顺丰");
                rpcOrder.setExpressId("1");
                return rpcOrder;
            });
            when(mockOrderRpcService.getOrderById(testOrderId)).thenReturn(testOrder);

            testOrder.setStatus(DataStatus.PAID.getStatus());
            ClassicHttpRequest updateOrderStatus = createRequestBuilder(Method.PATCH, "/api/v1/order", testOrder);
            try (CloseableHttpResponse response = httpclient.execute(updateOrderStatus)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<OrderResponse> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<OrderResponse>>() {
                });

                matchAllProperty(res.getData());
                assertEquals(DataStatus.PAID.getStatus(), res.getData().getStatus());
            }

            // 获取订单列表
            when(mockOrderRpcService.getOrderRpcService().getOrderListWithPageByUserId(anyInt(), anyInt(), any(), any())).thenAnswer(invocation -> {
                int pageNum = invocation.getArgument(0);
                int pageSize = invocation.getArgument(1);
                DataStatus status = invocation.getArgument(2);
                RpcOrder rpcOrder = new RpcOrder();
                rpcOrder.setId(testOrderId);
                rpcOrder.setGoodsIdAndNumberList(testGoodsIdAndNumberList);
                rpcOrder.setTotalPrice(16500L);
                int totalPage = 12;
                if (status == null) {
                    totalPage = 17;
                }
                return ResponseWithPages.response(pageNum, pageSize, totalPage, Collections.singletonList(rpcOrder));
            });

            ClassicHttpRequest getOrderList = createRequestBuilder(Method.GET, "/api/v1/order?pageNum=1&pageSize=2", null);
            try (CloseableHttpResponse response = httpclient.execute(getOrderList)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<OrderResponse>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<OrderResponse>>>() {
                });

                assertEquals(1, res.getData().size());
                assertEquals(1, res.getPageNum());
                assertEquals(2, res.getPageSize());
                assertEquals(17, res.getTotalPage());
            }

            // 获取状态是 PAID 的订单列表
            ClassicHttpRequest getOrderListWithStatus = createRequestBuilder(Method.GET, "/api/v1/order?pageNum=1&pageSize=2&status=paid", null);
            try (CloseableHttpResponse response = httpclient.execute(getOrderListWithStatus)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<OrderResponse>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<OrderResponse>>>() {
                });

                assertEquals(1, res.getData().size());
                assertEquals(1, res.getPageNum());
                assertEquals(2, res.getPageSize());
                assertEquals(12, res.getTotalPage());
                matchAllProperty(res.getData().get(0));
            }

            // 更新订单 => 物流 & 物流公司
            testOrder.setExpressCompany("顺丰");
            testOrder.setExpressId("1");
            ClassicHttpRequest updateOrderExpressInfo = createRequestBuilder(Method.PATCH, "/api/v1/order", testOrder);
            try (CloseableHttpResponse response = httpclient.execute(updateOrderExpressInfo)) {
                assertEquals(HTTP_OK, response.getCode());
                Response<OrderResponse> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<OrderResponse>>() {
                });

                matchAllProperty(res.getData());
                assertEquals("顺丰", res.getData().getExpressCompany());
                assertEquals("1", res.getData().getExpressId());
            }

            // 删除订单
            when(mockOrderRpcService.getOrderRpcService().deleteOrder(testOrderId, loggedUser.getId())).thenAnswer(invocation -> {
                long orderId = invocation.getArgument(0);
                RpcOrder rpcOrder = new RpcOrder();
                rpcOrder.setId(orderId);
                rpcOrder.setGoodsIdAndNumberList(testGoodsIdAndNumberList);
                return rpcOrder;
            });
            ClassicHttpRequest deleteOrder = createRequestBuilder(Method.DELETE, "/api/v1/order/" + testOrderId, null);
            try (CloseableHttpResponse response = httpclient.execute(deleteOrder)) {
                assertEquals(HTTP_NO_CONTENT, response.getCode());
            }

            // 获取状态是 PAID 的订单列表
            when(mockOrderRpcService.getOrderRpcService().getOrderListWithPageByUserId(anyInt(), anyInt(), any(), any())).thenAnswer(invocation -> {
                int pageNum = invocation.getArgument(0);
                int pageSize = invocation.getArgument(1);
                int totalPage = 1;
                return ResponseWithPages.response(pageNum, pageSize, totalPage, Collections.emptyList());
            });
            try (CloseableHttpResponse response = httpclient.execute(getOrderListWithStatus)) {
                assertEquals(HTTP_OK, response.getCode());
                ResponseWithPages<List<OrderResponse>> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<ResponseWithPages<List<OrderResponse>>>() {
                });
                assertEquals(0, res.getData().size());
            }
        }
    }

    private void matchAllProperty(OrderResponse orderResponse) {
        assertEquals(1234L, orderResponse.getId());
        assertEquals(1, orderResponse.getShop().getId());
        assertEquals(2, orderResponse.getGoodsList().size());
        assertEquals(Arrays.asList(1L, 2L), orderResponse.getGoodsList().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        assertEquals(Arrays.asList(6, 7), orderResponse.getGoodsList().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        assertEquals(16500, orderResponse.getTotalPrice());
    }

    @Test
    public void canRollBackIfDeductStockFailed() throws Exception {
        long testOrderId = 1234L;
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            User loggedUser = login(httpclient);

            List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(11, 1));
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(7, 2));

            assertHttpException(httpclient, Method.POST, "/api/v1/order", testGoodsIdAndNumberList, HTTP_GONE, "扣减库存失败！");

            // check rollback, can deduct stock
            when(mockOrderRpcService.getOrderRpcService().createOrder(any(), any())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(1);
                order.setId(testOrderId);
                order.setStatus(DataStatus.PENDING.getStatus());
                return order;
            });

            testGoodsIdAndNumberList.get(0).setNumber(6);

            ClassicHttpRequest createOrder = createRequestBuilder(Method.POST, "/api/v1/order", testGoodsIdAndNumberList);
            try (CloseableHttpResponse response = httpclient.execute(createOrder)) {
                assertEquals(HTTP_CREATED, response.getCode());
                Response<OrderResponse> res = objectMapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<Response<OrderResponse>>() {
                });

                matchAllProperty(res.getData());
                assertEquals(loggedUser.getId(), res.getData().getUserId());
                assertEquals(DataStatus.PENDING.getStatus(), res.getData().getStatus());
            }
        }
    }

    @Test
    public void testInvalidStatusWhenGetOrderList() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            assertHttpException(httpclient, Method.GET, "/api/v1/order?pageNum=1&pageSize=2&status=delete", null, HTTP_BAD_REQUEST, "非法 status：delete");
        }
    }

    @Test
    public void testOrderIdIsNullWhenUpdateOrder() throws Exception {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/order", new Order(), HTTP_BAD_REQUEST, "非法 orderId");
        }
    }

    @Test
    public void testInvalidOrderWhenUpdateOrder() throws Exception {
        long testOrderId = 1234L;
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);

            // not found
            when(mockOrderRpcService.getOrderById(testOrderId)).thenReturn(null);
            Order testOrder = new Order();
            testOrder.setId(testOrderId);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/order", testOrder, HTTP_NOT_FOUND, "订单未找到，orderId：1234");

            // forbidden
            testOrder.setShopId(3L);
            when(mockOrderRpcService.getOrderById(testOrderId)).thenReturn(testOrder);
            assertHttpException(httpclient, Method.PATCH, "/api/v1/order", testOrder, HTTP_FORBIDDEN, "无权访问！");
        }
    }

    @Test
    public void testInvalidNumberWhenCreateOrder() throws Exception {
        long testOrderId = 1234L;
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            login(httpclient);
            List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
            testGoodsIdAndNumberList.add(new GoodsIdAndNumber(-1, 1));

            when(mockOrderRpcService.getOrderRpcService().createOrder(any(), any())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(1);
                order.setId(testOrderId);
                order.setStatus(DataStatus.PENDING.getStatus());
                return order;
            });
            assertHttpException(httpclient, Method.POST, "/api/v1/order", testGoodsIdAndNumberList, HTTP_BAD_REQUEST, "数量非法：-1");
        }
    }
}
