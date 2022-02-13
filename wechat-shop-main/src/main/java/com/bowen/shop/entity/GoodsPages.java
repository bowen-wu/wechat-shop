package com.bowen.shop.entity;

import com.bowen.shop.api.entity.Pages;

public class GoodsPages extends Pages {
    private Long shopId;

    public GoodsPages(int pageNum, int pageSize, Long shopId) {
        super(pageNum, pageSize);
        this.shopId = shopId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}
