package com.bowen.shop.api.rpc;

import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.generate.Order;

import java.util.List;

public interface OrderRpcService {
    Order createOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, Order order);
}
