package com.bowen.shop.order.dao;

import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.generate.OrderGoods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomOrderGoodsMapper {
    void batchInsert(List<OrderGoods> orderGoodsList);

    void updateOrder(Order order);

    Order selectByPrimaryKey(Long id);
}
