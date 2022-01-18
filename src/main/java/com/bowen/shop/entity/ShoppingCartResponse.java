package com.bowen.shop.entity;

import com.bowen.shop.generate.Shop;

import java.util.List;

public class ShoppingCartResponse {
    private Shop shop;
    private List<GoodsWithNumber> goodsWithNumberList;

    public static ShoppingCartResponse of(Shop shop, List<GoodsWithNumber> goodsWithNumberList) {
        return new ShoppingCartResponse(shop, goodsWithNumberList);
    }

    private ShoppingCartResponse(Shop shop, List<GoodsWithNumber> goodsWithNumberList) {
        this.shop = shop;
        this.goodsWithNumberList = goodsWithNumberList;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<GoodsWithNumber> getGoodsWithNumberList() {
        return goodsWithNumberList;
    }

    public void setGoodsWithNumberList(List<GoodsWithNumber> goodsWithNumberList) {
        this.goodsWithNumberList = goodsWithNumberList;
    }
}
