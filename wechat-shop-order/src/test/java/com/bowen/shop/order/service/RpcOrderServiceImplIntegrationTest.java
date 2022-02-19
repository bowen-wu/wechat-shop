package com.bowen.shop.order.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.HttpException;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.order.generate.OrderGoodsMapper;
import com.bowen.shop.order.generate.OrderMapper;
import com.bowen.shop.order.dao.CustomOrderGoodsMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO: integrationTest Or unitTest
// 不启动 Spring，通过读取 Mybatis 配置，创建 SqlSession 实例，之后创建 rpcOrderService
class RpcOrderServiceImplIntegrationTest {
    String databaseUrl = "jdbc:mysql://10.40.95.56:3307/order";
    String databaseUsername = "root";
    String databasePassword = "my-secret-pw";

    RpcOrderServiceImpl rpcOrderService;

    SqlSession sqlSession;

    @BeforeEach
    public void setUpDatabase() throws IOException {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();

        // 读取 Mybatis 配置，创建 SqlSession 实例
        String resource = "db/test-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession(true);

        rpcOrderService = new RpcOrderServiceImpl(
                sqlSession.getMapper(OrderMapper.class),
                sqlSession.getMapper(OrderGoodsMapper.class),
                sqlSession.getMapper(CustomOrderGoodsMapper.class)
        );
    }

    @AfterEach
    public void cleanUp() {
        sqlSession.close();
    }

    @Test
    public void testOrderLifecycle() {
        List<GoodsIdAndNumber> testGoodsIdAndNumberList = new ArrayList<>();
        testGoodsIdAndNumberList.add(new GoodsIdAndNumber(6, 1));
        testGoodsIdAndNumberList.add(new GoodsIdAndNumber(7, 2));

        Order order4Test = new Order();
        order4Test.setUserId(1L);
        order4Test.setShopId(1L);
        order4Test.setAddress("火星");
        order4Test.setTotalPrice(16500L);

        // create order
        Order createdOrder = rpcOrderService.createOrder(testGoodsIdAndNumberList, order4Test);
        assertNotNull(createdOrder.getId());
        assertEquals(DataStatus.PENDING.getStatus(), createdOrder.getStatus());

        // update order
        createdOrder.setStatus(DataStatus.PAID.getStatus());
        createdOrder.setExpressCompany("顺丰");
        createdOrder.setExpressId("101");

        RpcOrder updatedOrder = rpcOrderService.updateOrder(createdOrder);
        matchAllProperty(createdOrder, order4Test, updatedOrder);
        assertEquals("101", updatedOrder.getExpressId());
        assertEquals(createdOrder.getStatus(), updatedOrder.getStatus());
        assertEquals(2, updatedOrder.getGoodsIdAndNumberList().size());
        assertEquals(Arrays.asList(1L, 2L), updatedOrder.getGoodsIdAndNumberList().stream().map(GoodsIdAndNumber::getId).collect(toList()));
        assertEquals(Arrays.asList(6, 7), updatedOrder.getGoodsIdAndNumberList().stream().map(GoodsIdAndNumber::getNumber).collect(toList()));

        // get order
        Order orderById = rpcOrderService.getOrderById(updatedOrder.getId());
        matchAllProperty(createdOrder, order4Test, orderById);
        assertEquals(createdOrder.getStatus(), orderById.getStatus());

        // delete order
        RpcOrder deletedOrder = rpcOrderService.deleteOrder(orderById.getId(), order4Test.getUserId());
        matchAllProperty(createdOrder, order4Test, deletedOrder);
        assertEquals(DataStatus.DELETED.getStatus(), deletedOrder.getStatus());
        assertEquals(0, deletedOrder.getGoodsIdAndNumberList().size());

        // get order => null
        Order orderForDeleted = rpcOrderService.getOrderById(deletedOrder.getId());
        assertNull(orderForDeleted);
    }

    private <T extends Order> void matchAllProperty(Order createdOrder, Order order4Test, T orderForAssert) {
        assertEquals(createdOrder.getExpressCompany(), orderForAssert.getExpressCompany());
        assertEquals(createdOrder.getId(), orderForAssert.getId());
        assertEquals(order4Test.getUserId(), orderForAssert.getUserId());
        assertEquals(order4Test.getShopId(), orderForAssert.getShopId());

        assertEquals(order4Test.getTotalPrice(), orderForAssert.getTotalPrice());
        assertEquals(order4Test.getAddress(), orderForAssert.getAddress());
    }

    @Test
    public void testGetOrderList() {
        ResponseWithPages<List<RpcOrder>> orderListWithPageByUserId = rpcOrderService.getOrderListWithPageByUserId(2, 2, null, 1L);
        assertEquals(2, orderListWithPageByUserId.getPageNum());
        assertEquals(2, orderListWithPageByUserId.getPageSize());
        assertEquals(3, orderListWithPageByUserId.getTotalPage());
        assertEquals(2, orderListWithPageByUserId.getData().size());
        assertEquals(Arrays.asList(3L, 4L), orderListWithPageByUserId.getData().stream().map(RpcOrder::getId).collect(toList()));
        assertEquals(Arrays.asList(1L, 1L), orderListWithPageByUserId.getData().stream().map(RpcOrder::getUserId).collect(toList()));
        assertEquals(Arrays.asList(1100L, 1200L), orderListWithPageByUserId.getData().stream().map(RpcOrder::getTotalPrice).collect(toList()));
        assertEquals(Arrays.asList(2L, 3L, 4L), orderListWithPageByUserId
                .getData()
                .stream()
                .map(RpcOrder::getGoodsIdAndNumberList)
                .flatMap(Collection::stream)
                .map(GoodsIdAndNumber::getId)
                .collect(toList()));
        assertEquals(Arrays.asList(1, 2, 4), orderListWithPageByUserId
                .getData()
                .stream()
                .map(RpcOrder::getGoodsIdAndNumberList)
                .flatMap(Collection::stream)
                .map(GoodsIdAndNumber::getNumber)
                .collect(toList()));
    }

    @Test
    public void throwExceptionWhenDeleteOrder() {
        HttpException notFoundException = assertThrows(HttpException.class, () -> {
            rpcOrderService.deleteOrder(9999L, 2L);
        });
        assertEquals(HTTP_NOT_FOUND, notFoundException.getStatusCode());
        assertEquals("订单ID非法，订单ID：9999", notFoundException.getMessage());

        HttpException forbiddenException = assertThrows(HttpException.class, () -> {
            rpcOrderService.deleteOrder(1L, 2L);
        });
        assertEquals(HTTP_FORBIDDEN, forbiddenException.getStatusCode());
        assertEquals("无权访问！", forbiddenException.getMessage());
    }

    @Test
    public void badRequestWhenCreateOrder() {
        List<GoodsIdAndNumber> testGoodsIdAndNumberList = Collections.singletonList(GoodsIdAndNumber.of(2, 1));

        Order order4Test = new Order();
        order4Test.setShopId(1L);

        HttpException addressException = assertThrows(HttpException.class, () -> {
            rpcOrderService.createOrder(testGoodsIdAndNumberList, order4Test);
        });
        assertEquals(HTTP_BAD_REQUEST, addressException.getStatusCode());
        assertEquals("address 不能为空!", addressException.getMessage());


        order4Test.setAddress("火星");
        HttpException totalPriceException = assertThrows(HttpException.class, () -> {
            rpcOrderService.createOrder(testGoodsIdAndNumberList, order4Test);
        });
        assertEquals(HTTP_BAD_REQUEST, totalPriceException.getStatusCode());
        assertEquals("totalPrice非法!", totalPriceException.getMessage());

        order4Test.setTotalPrice(2000L);
        HttpException userIdException = assertThrows(HttpException.class, () -> {
            rpcOrderService.createOrder(testGoodsIdAndNumberList, order4Test);
        });
        assertEquals(HTTP_BAD_REQUEST, userIdException.getStatusCode());
        assertEquals("userId 不能为空!", userIdException.getMessage());
    }
}
