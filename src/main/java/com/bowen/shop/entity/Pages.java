package com.bowen.shop.entity;

public class Pages {
    private int pageNum;
    private int pageSize;

    public Pages() {
    }

    public Pages(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }
}
