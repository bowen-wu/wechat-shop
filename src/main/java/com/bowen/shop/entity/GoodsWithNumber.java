package com.bowen.shop.entity;

import com.bowen.shop.generate.Goods;

public class GoodsWithNumber extends Goods {
    private int number;

    public GoodsWithNumber() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
