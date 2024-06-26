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
public class DummyBankRequest {
    @NotBlank
    private String cardNumber;
    @NotBlank
    private String cvv;
    @NotBlank
    private String holderName;
    @NotBlank
    private String expDate;
}
