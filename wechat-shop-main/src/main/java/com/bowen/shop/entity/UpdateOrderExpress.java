package com.bowen.shop.entity;

public class UpdateOrderExpress extends UpdateOrderInfo {
    private String expressCompany;
    private String expressId;

    public UpdateOrderExpress() {
    }

    public UpdateOrderExpress(long orderId, String expressCompany, String expressId) {
        super(orderId);
        this.expressCompany = expressCompany;
        this.expressId = expressId;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getExpressId() {
        return expressId;
    }

    public void setExpressId(String expressId) {
        this.expressId = expressId;
    }
}
