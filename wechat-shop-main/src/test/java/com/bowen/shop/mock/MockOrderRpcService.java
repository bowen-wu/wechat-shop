package com.bowen.shop.mock;

import com.bowen.shop.api.entity.GoodsIdAndNumber;
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

    public OrderRpcService getOrderRpcService() {
        return orderRpcService;
    }
}
