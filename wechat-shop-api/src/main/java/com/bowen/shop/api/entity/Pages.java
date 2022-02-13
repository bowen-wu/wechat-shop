package com.bowen.shop.api.entity;

import java.io.Serializable;

public class Pages implements Serializable {
    private int pageNum;
    private int pageSize;
    public static final int DEFAULT_PAGE_SIZE = 10;

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

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
