package com.bowen.shop.order.service;

import com.bowen.shop.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${shop.orderService.version}")
public class RpcOrderServiceImpl implements OrderRpcService {
    @Override
    public void placeOrder(int goodsId, int number) {

    }
}
