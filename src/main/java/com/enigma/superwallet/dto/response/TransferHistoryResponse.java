package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransferHistoryResponse {
    private AccountResponse source;
    private AccountResponse destination;
    private String totalAmount;
    private LocalDateTime date;
    private String withdrawalCode;
}
