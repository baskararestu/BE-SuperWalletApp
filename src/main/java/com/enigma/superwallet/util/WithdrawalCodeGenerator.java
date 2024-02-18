package com.enigma.superwallet.util;
import java.util.UUID;

public class WithdrawalCodeGenerator {

    public static String generateUniqueWithdrawalCode() {
        UUID uuid = UUID.randomUUID();
        // Convert UUID to a string and remove hyphens
        String withdrawalCode = uuid.toString().replaceAll("-", "");
        // Truncate the string to a desired length (e.g., 10 characters)
        withdrawalCode = withdrawalCode.substring(0, Math.min(withdrawalCode.length(), 20));

        return withdrawalCode;
    }
}
