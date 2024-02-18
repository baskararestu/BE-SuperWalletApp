package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransferHistoryDetailsResponse {
    private String firstName;
    private String lastName;
    private String accountNumber;
    private String currencyCode;
    private String currencyName;
}
