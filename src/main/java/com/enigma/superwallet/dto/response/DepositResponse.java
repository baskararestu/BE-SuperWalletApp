package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.constant.ECurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DepositResponse {
    private String transactionId;
    private String customerId;
    private String amount;
    private ECurrencyCode currency;
    private String accountId;
    private String newBalance;
}
