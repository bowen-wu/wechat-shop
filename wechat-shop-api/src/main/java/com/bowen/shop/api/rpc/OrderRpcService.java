package com.bowen.shop.api.rpc;

public interface OrderRpcService {
    void sayHello(String name);

    void placeOrder(int goodsId, int number);
}
