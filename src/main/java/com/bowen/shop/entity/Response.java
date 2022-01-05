package com.bowen.shop.entity;

public class Response<T> {
    private T data;

    public Response() {
    }

    public static <R> Response<R> of(R data) {
        return new Response<>(data);
    }

    private Response(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
