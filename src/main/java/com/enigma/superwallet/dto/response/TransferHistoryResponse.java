package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransferHistoryResponse {
    private TransferHistoryDetailsResponse source;
    private TransferHistoryDetailsResponse destination;
    private String totalAmount;
    private String date;
    private String transactionType;
    private BigDecimal totalFee;
    private String withdrawalCode;
}
