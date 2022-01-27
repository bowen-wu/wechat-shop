package com.bowen.shop.entity;

public abstract class UpdateOrderInfo {
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
