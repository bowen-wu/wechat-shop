package com.bowen.shop.mock;

import com.bowen.shop.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${shop.orderService.version}")
public class MockOrderRpcService implements OrderRpcService {
    @Override
    public void placeOrder(int goodsId, int number) {
        System.out.println("I am mock orderRpcService");
    }
}
