package com.enigma.superwallet.util;

import com.enigma.superwallet.constant.ECurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationCurrencyCode {
    public static boolean isValidCurrencyCode(String currencyCode) {
        for (ECurrencyCode code : ECurrencyCode.values()) {
            if (code.name().equals(currencyCode)) {
                return true;
            }
        }
        return false;
    }
}
