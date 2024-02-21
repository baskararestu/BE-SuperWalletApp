package com.enigma.superwallet.util;
import java.util.UUID;

public class WithdrawalCodeGenerator {

    public static String generateUniqueWithdrawalCode() {
        UUID uuid = UUID.randomUUID();
        String withdrawalCode = uuid.toString().replaceAll("-", "");
        withdrawalCode = withdrawalCode.substring(0, Math.min(withdrawalCode.length(), 20));

        return withdrawalCode;
    }
}
