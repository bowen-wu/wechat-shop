package com.bowen.shop.mock;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.mockito.Mock;

import java.util.List;

@DubboService(version = "${shop.orderService.version}")
public class MockOrderRpcService implements OrderRpcService {
    @Mock
    OrderRpcService orderRpcService;

    @Override
    public Order createOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, Order order) {
        return orderRpcService.createOrder(goodsIdAndNumberList, order);
    }

    @Override
    public RpcOrder deleteOrder(long orderId, long userId) {
        return orderRpcService.deleteOrder(orderId, userId);
    }

    @Override
    public ResponseWithPages<List<RpcOrder>> getOrderListWithPageByUserId(int pageNum, int pageSize, DataStatus status, Long userId) {
        return orderRpcService.getOrderListWithPageByUserId(pageNum, pageSize, status, userId);
    }

    @Override
    public Order getOrderById(long orderId) {
        return orderRpcService.getOrderById(orderId);
    }

    @Override
    public RpcOrder updateOrder(Order order) {
        return orderRpcService.updateOrder(order);
    }

    public OrderRpcService getOrderRpcService() {
        return orderRpcService;
    }
}
