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
        this.setId(order.getId());
        this.setUpdatedAt(order.getUpdatedAt());
        this.setStatus(order.getStatus());
        this.setAddress(order.getAddress());
        this.setUserId(order.getUserId());
        this.setExpressId(order.getExpressId());
        this.setExpressCompany(order.getExpressCompany());
        this.setCreatedAt(order.getCreatedAt());
        this.setTotalPrice(order.getTotalPrice());
    }

    public List<GoodsIdAndNumber> getGoodsIdAndNumberList() {
        return goodsIdAndNumberList;
    }

    public void setGoodsIdAndNumberList(List<GoodsIdAndNumber> goodsIdAndNumberList) {
        this.goodsIdAndNumberList = goodsIdAndNumberList;
    }
}
