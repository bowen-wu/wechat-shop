package com.bowen.shop.controller;

import com.bowen.shop.api.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @DubboReference(
            version = "${shop.orderservice.version}",
            url = "${shop.orderservice.url}"
    )
    private OrderRpcService orderRpcService;

    @GetMapping("/testRpc")
    public void testRpc() {
        System.out.println(111);
        orderRpcService.placeOrder(1, 2);
    }
}
