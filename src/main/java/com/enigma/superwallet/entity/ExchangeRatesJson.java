package com.enigma.superwallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExchangeRatesJson {

    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;
}

