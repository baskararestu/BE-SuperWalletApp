package com.enigma.superwallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransferRequest {
    private String fromNumber;
    private Double amountTransfer;
    private String toNumber;
    private String pin;
}
