package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransferResponse {
    private String from;
    private String fromNumber;
    private String fromCurrency;
    private String to;
    private String toNumber;
    private String toCurrency;
    private String totalAmount;
    private String fee;
}
