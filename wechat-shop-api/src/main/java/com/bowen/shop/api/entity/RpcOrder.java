package com.bowen.shop.api.entity;

import com.bowen.shop.api.generate.Order;

import java.io.Serializable;
import java.util.List;

public class RpcOrder extends Order implements Serializable {
    private List<GoodsIdAndNumber> goodsIdAndNumberList;

    public RpcOrder() {
    }

    public static RpcOrder of(Order order, List<GoodsIdAndNumber> goodsIdAndNumberList) {
        RpcOrder rpcOrder = new RpcOrder(order);
        rpcOrder.setGoodsIdAndNumberList(goodsIdAndNumberList);
        return rpcOrder;
    }

    public RpcOrder(List<GoodsIdAndNumber> goodsIdAndNumberList) {
        this.goodsIdAndNumberList = goodsIdAndNumberList;
    }

    public RpcOrder(Order order) {
        RpcOrder result = new RpcOrder();
        result.setId(order.getId());
        result.setUpdatedAt(order.getUpdatedAt());
        result.setStatus(order.getStatus());
        result.setAddress(order.getAddress());
        result.setUserId(order.getUserId());
        result.setExpressId(order.getExpressId());
        result.setExpressCompany(order.getExpressCompany());
        result.setCreatedAt(order.getCreatedAt());
        result.setTotalPrice(order.getTotalPrice());
    }

    public List<GoodsIdAndNumber> getGoodsIdAndNumberList() {
        return goodsIdAndNumberList;
    }

    public void setGoodsIdAndNumberList(List<GoodsIdAndNumber> goodsIdAndNumberList) {
        this.goodsIdAndNumberList = goodsIdAndNumberList;
    }
}
