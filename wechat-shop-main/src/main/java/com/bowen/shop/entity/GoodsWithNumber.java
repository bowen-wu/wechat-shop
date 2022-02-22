package com.bowen.shop.entity;

import com.bowen.shop.generate.Goods;

public class GoodsWithNumber extends Goods {
    private int number;

    public GoodsWithNumber() {
    }

    public static GoodsWithNumber of(Goods goods, int number) {
        GoodsWithNumber goodsWithNumber = new GoodsWithNumber(goods);
        goodsWithNumber.setNumber(number);
        return goodsWithNumber;
    }

    public GoodsWithNumber(Goods goods) {
        this.setCreatedAt(goods.getCreatedAt());
        this.setDescription(goods.getDescription());
        this.setDetails(goods.getDetails());
        this.setId(goods.getId());
        this.setImageUrl(goods.getImageUrl());
        this.setName(goods.getName());
        this.setPrice(goods.getPrice());
        this.setStock(goods.getStock());
        this.setStatus(goods.getStatus());
        this.setShopId(goods.getShopId());
        this.setUpdatedAt(goods.getUpdatedAt());
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
