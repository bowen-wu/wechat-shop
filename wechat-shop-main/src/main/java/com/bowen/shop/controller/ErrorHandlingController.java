package com.bowen.shop.controller;

import com.bowen.shop.api.entity.HttpException;
import com.bowen.shop.entity.Response;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ErrorHandlingController {
    @ExceptionHandler(HttpException.class)
    @ResponseBody
    public Response<?> onError(HttpServletResponse response, HttpException exception) {
        response.setStatus(exception.getStatusCode());
        return Response.fail(exception.getMessage());
    }

}
