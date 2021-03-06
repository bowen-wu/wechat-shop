package com.bowen.shop.api.entity;

import java.io.Serializable;
import java.util.List;

public class ResponseWithPages<T> extends Pages implements Serializable {
    private int totalPage;
    private T data;

    public ResponseWithPages() {
        super();
    }

    public static <R> ResponseWithPages<R> response(int pageNum, int pageSize, int totalPage, R data) {
        ResponseWithPages<R> response = new ResponseWithPages<>();
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotalPage(totalPage);
        response.setData(data);
        return response;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public T getData() {
        return data;
    }
}
