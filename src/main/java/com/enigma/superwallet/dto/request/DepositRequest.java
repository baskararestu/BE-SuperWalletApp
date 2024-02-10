package com.enigma.superwallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DepositRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String dummyBankId;

    @NotBlank
    private String accountId;

    @NotBlank
    private String pin;

    @NotBlank
    private Double amount;
}
