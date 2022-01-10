package com.bowen.shop.entity;

public class ResponseWithPages<T> extends Pages {
    private final int totalPage;
    private final T data;

    public ResponseWithPages(int pageNum, int pageSize, int totalPage, T data) {
        super(pageNum, pageSize);
        this.totalPage = totalPage;
        this.data = data;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public T getData() {
        return data;
    }
}
