package com.bowen.shop.api.entity;

public enum DataStatus {
    OK("ok"),
    DELETED("deleted"),

    // only for order
    PENDING("pending"),
    PAID("paid"),
    DELIVERED("delivered"),
    RECEIVED("received");

    private final String status;

    DataStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
