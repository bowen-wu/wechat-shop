package com.bowen.shop.order.dao;

import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.generate.OrderGoods;

import java.util.List;

public interface CustomOrderGoodsMapper {
    void batchInsert(List<OrderGoods> orderGoodsList);

    void updateOrder(Order order);

    Order selectByPrimaryKey(Long id);
}
