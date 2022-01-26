package com.bowen.shop.controller;

import com.bowen.shop.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @DubboReference(
            version = "${shop.orderService.version}",
            url = "${shop.orderService.url}"
    )
    private OrderRpcService orderRpcService;

    @GetMapping("/testRpc")
    public void testRpc() {
        System.out.println(111);
        orderRpcService.sayHello("consumer");
    }
}
