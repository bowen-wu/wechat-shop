package com.bowen.shop.entity;

import com.bowen.shop.generate.Shop;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
public class ShoppingCartData {
    private Shop shop;
    private List<GoodsWithNumber> goodsWithNumberList;

    public ShoppingCartData() {
    }

    public static ShoppingCartData of(Shop shop, List<GoodsWithNumber> goodsWithNumberList) {
        return new ShoppingCartData(shop, goodsWithNumberList);
    }

    private ShoppingCartData(Shop shop, List<GoodsWithNumber> goodsWithNumberList) {
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
