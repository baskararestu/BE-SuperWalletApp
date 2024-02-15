package com.enigma.superwallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse {
    private String date; // Date in "yyyy-MM-dd" format
    private String base;
    private String currency;
    private BigDecimal rate; // BigDecimal format for rate
}
