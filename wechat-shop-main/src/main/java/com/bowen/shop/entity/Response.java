package com.bowen.shop.entity;

public class Response<T> {
    private T data;
    private String message;

    public Response() {
    }

    public static <R> Response<R> success(R data) {
        return new Response<>(data, "success");
    }

    public static <R> Response<R> fail(String message) {
        return new Response<>(null, message);
    }

    public static <R> Response<R> of(R data, String message) {
        return new Response<>(data, message);
    }

    private Response(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
