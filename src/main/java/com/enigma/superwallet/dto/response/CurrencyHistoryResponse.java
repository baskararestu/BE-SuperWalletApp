package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.entity.CurrencyHistory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CurrencyHistoryResponse {
    private String date; // Date in "yyyy-MM-dd" format
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal rate; // BigDecimal format for rate

    public CurrencyHistoryResponse(CurrencyHistory currencyHistory) {
        this.date = convertUnixTimestampToLocalDate(currencyHistory.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.baseCurrency = currencyHistory.getBase();
        this.targetCurrency = currencyHistory.getCurrency().getCode().name();
        this.rate = BigDecimal.valueOf(currencyHistory.getRate());
    }

    private LocalDate convertUnixTimestampToLocalDate(long unixTimestamp) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(unixTimestamp), ZoneId.systemDefault());
        return zonedDateTime.toLocalDate();
    }
}
