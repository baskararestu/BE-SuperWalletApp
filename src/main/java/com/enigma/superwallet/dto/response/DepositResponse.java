package com.enigma.superwallet.dto.response;

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
    private String accountId;
    private Double newBalance;
}
