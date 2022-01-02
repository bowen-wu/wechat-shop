package com.bowen.shop.service;

import com.bowen.shop.entity.Tel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TelVerificationServiceTest {
    private static final Tel EMPTY_TEL = new Tel(null);
    public static final Tel VALID_PARAMETER = new Tel("13012341234");
    public static final Tel INVALID_PARAMETER = new Tel("1301234123");

    @Test
    public void returnFalseIfInvalid() {
        assertFalse(new TelVerificationService().verifyTelParameter(null));
        assertFalse(new TelVerificationService().verifyTelParameter(EMPTY_TEL));
        assertFalse(new TelVerificationService().verifyTelParameter(INVALID_PARAMETER));
    }

    @Test
    public void returnTrueIfValid() {
        assertTrue(new TelVerificationService().verifyTelParameter(VALID_PARAMETER));
    }
}
