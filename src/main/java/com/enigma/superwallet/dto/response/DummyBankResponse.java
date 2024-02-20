package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DummyBankResponse {
    private String id;
    private String bankNumber;
    private String accountNumber;
}
