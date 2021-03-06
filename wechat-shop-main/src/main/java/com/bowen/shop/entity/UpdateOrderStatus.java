package com.bowen.shop.entity;

import com.bowen.shop.api.entity.DataStatus;

public class UpdateOrderStatus extends UpdateOrderInfo {
    private DataStatus status;

    public UpdateOrderStatus() {
    }

    public UpdateOrderStatus(long orderId, DataStatus status) {
        super(orderId);
        this.status = status;
    }

    public DataStatus getStatus() {
        return status;
    }

    public void setStatus(DataStatus status) {
        this.status = status;
    }
}
