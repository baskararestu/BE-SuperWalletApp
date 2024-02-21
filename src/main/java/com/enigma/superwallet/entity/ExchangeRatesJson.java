package com.enigma.superwallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExchangeRatesJson {
    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;
    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;
}

