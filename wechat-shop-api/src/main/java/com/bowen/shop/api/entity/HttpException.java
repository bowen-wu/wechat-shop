package com.bowen.shop.api.entity;

import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {
    private final int statusCode;

    public static HttpException gone(String message) {
        return new HttpException(message, HttpStatus.GONE.value());
    }

    public static HttpException badRequest(String message) {
        return new HttpException(message, HttpStatus.BAD_REQUEST.value());
    }

    public static HttpException notFound(String message) {
        return new HttpException(message, HttpStatus.NOT_FOUND.value());
    }

    public static HttpException forbidden(String message) {
        return new HttpException(message, HttpStatus.FORBIDDEN.value());
    }

    private HttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }


    public int getStatusCode() {
        return statusCode;
    }
}
