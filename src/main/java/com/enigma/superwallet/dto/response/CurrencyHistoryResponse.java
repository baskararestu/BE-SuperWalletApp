package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.entity.CurrencyHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyHistoryResponse {
    private String date; // Date in "yyyy-MM-dd" format
    private String base;
    private String currency;
    private BigDecimal rate; // BigDecimal format for rate

    public CurrencyHistoryResponse(CurrencyHistory currencyHistory) {
        this.date = convertUnixTimestampToLocalDate(currencyHistory.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.base = currencyHistory.getBase();
        this.currency = currencyHistory.getCurrency().getCode().name();
        this.rate = BigDecimal.valueOf(currencyHistory.getRate());
    }

    private LocalDate convertUnixTimestampToLocalDate(long unixTimestamp) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(unixTimestamp), ZoneId.systemDefault());
        return zonedDateTime.toLocalDate();
    }
}
