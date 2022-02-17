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

    public static DataStatus fromStatus(String name) {
        try {
            if (name == null) {
                return null;
            }
            return DataStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
