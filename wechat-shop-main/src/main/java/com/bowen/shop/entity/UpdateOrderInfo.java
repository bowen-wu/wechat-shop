package com.bowen.shop.entity;

abstract public class UpdateOrderInfo {
    private long orderId;

    public UpdateOrderInfo() {
    }

    public UpdateOrderInfo(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
