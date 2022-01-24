package com.bowen.shop.mock;

import com.bowen.shop.api.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${shop.orderservice.version}")
public class MockOrderRpcService implements OrderRpcService {
    @Override
    public void placeOrder(int goodsId, int number) {
        System.out.println("I am mock orderRpcService");
    }
}
