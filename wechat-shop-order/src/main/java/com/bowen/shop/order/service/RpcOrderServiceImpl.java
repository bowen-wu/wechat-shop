package com.bowen.shop.order.service;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.generate.Order;
import com.bowen.shop.api.generate.OrderGoods;
import com.bowen.shop.api.generate.OrderMapper;
import com.bowen.shop.api.rpc.OrderRpcService;
import com.bowen.shop.order.dao.CustomOrderGoodsMapper;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@DubboService(version = "${shop.orderService.version}")
public class RpcOrderServiceImpl implements OrderRpcService {
    private final OrderMapper orderMapper;
    private final CustomOrderGoodsMapper customOrderGoodsMapper;

    @Autowired
    public RpcOrderServiceImpl(OrderMapper orderMapper, CustomOrderGoodsMapper customOrderGoodsMapper) {
        this.orderMapper = orderMapper;
        this.customOrderGoodsMapper = customOrderGoodsMapper;
    }

    @Override
    public Order createOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, Order order) {
        insertOrder(order);
        List<OrderGoods> orderGoodsList = goodsIdAndNumberList.stream()
                .map(goodsIdAndNumber -> convertToOrderGoods(goodsIdAndNumber.getId(), goodsIdAndNumber.getNumber(), order.getId()))
                .collect(Collectors.toList());
        customOrderGoodsMapper.batchInsert(orderGoodsList);
        return order;
    }

    private OrderGoods convertToOrderGoods(long goodsId, int number, long orderId) {
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setGoodsId(goodsId);
        orderGoods.setNumber((long) number);
        orderGoods.setOrderId(orderId);
        return orderGoods;
    }


    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getStatus());

        verify(() -> StringUtils.isBlank(order.getAddress()), "address不能为空!");
        verify(() -> order.getTotalPrice() == null || order.getTotalPrice().compareTo(0L) < 0, "totalPrice非法!");
        verify(() -> order.getUserId() == null, "address不能为空!");

        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        order.setExpressId(null);
        order.setExpressCompany(null);
        orderMapper.insert(order);
    }

    private void verify(BooleanSupplier supplier, String message) {
        if (supplier.getAsBoolean()) {
            throw new IllegalArgumentException(message);
        }
    }
}
