package com.bowen.shop.entity;

public class Pages {
    private final int pageNum;
    private final int pageSize;

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
