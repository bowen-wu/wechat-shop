package com.bowen.shop.service;

import com.bowen.shop.api.rpc.OrderRpcService;
import com.bowen.shop.entity.GoodsIdAndNumber;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    @DubboReference(
            version = "${shop.orderService.version}",
            url = "${shop.orderService.url}"
    )
    private OrderRpcService orderRpcService;

    public void placeOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, long userId) {

    }
}
