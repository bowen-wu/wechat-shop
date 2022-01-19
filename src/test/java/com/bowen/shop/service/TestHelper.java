package com.bowen.shop.service;

import com.bowen.shop.entity.HttpException;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestHelper {
    public static void assertHttpException(Executable executable, HttpStatus httpStatus, String errorMessage) {
        HttpException httpException = assertThrows(HttpException.class, executable);

        assertEquals(httpStatus.value(), httpException.getStatusCode());
        assertEquals(errorMessage, httpException.getMessage());
    }
}
