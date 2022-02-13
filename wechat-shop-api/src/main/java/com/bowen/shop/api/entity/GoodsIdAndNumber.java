package com.bowen.shop.api.entity;

import java.io.Serializable;

public class GoodsIdAndNumber implements Serializable {
    private int number;
    private long id;

    public static GoodsIdAndNumber of(int number, long id) {
        return new GoodsIdAndNumber(number, id);
    }

    public GoodsIdAndNumber(int number, long id) {
        this.number = number;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
