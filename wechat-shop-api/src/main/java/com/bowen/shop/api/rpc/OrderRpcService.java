package com.bowen.shop.api.rpc;

import com.bowen.shop.api.entity.DataStatus;
import com.bowen.shop.api.entity.GoodsIdAndNumber;
import com.bowen.shop.api.entity.ResponseWithPages;
import com.bowen.shop.api.entity.RpcOrder;
import com.bowen.shop.api.generate.Order;

import java.util.List;

public interface OrderRpcService {
    Order createOrder(List<GoodsIdAndNumber> goodsIdAndNumberList, Order order);

    RpcOrder deleteOrder(long orderId, long userId);

    ResponseWithPages<List<RpcOrder>> getOrderListWithPageByUserId(int pageNum, int pageSize, DataStatus status, Long userId);

    Order getOrderById(long orderId);

    RpcOrder updateOrder(Order order);
}
