package com.bowen.shop.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MockSmsCodeServiceTest {
    @Test
    public void return000000() {
        Assertions.assertEquals("000000", new MockSmsCodeService().sendSmsCode("17900000000"));
    }
}
