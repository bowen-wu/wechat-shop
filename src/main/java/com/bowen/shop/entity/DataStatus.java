package com.bowen.shop.entity;

public enum DataStatus {
    OK("ok"),
    FAIL("fail");

    private final String status;

    DataStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
