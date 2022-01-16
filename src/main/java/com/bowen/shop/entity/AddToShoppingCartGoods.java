package com.bowen.shop.entity;

public class AddToShoppingCartGoods {
    private int number;
    private long id;

    public AddToShoppingCartGoods(int number, long id) {
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
