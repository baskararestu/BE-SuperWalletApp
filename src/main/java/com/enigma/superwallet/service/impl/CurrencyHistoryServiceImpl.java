package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.response.CurrencyHistoryResponse;
import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.CurrencyHistory;
import com.enigma.superwallet.entity.ExchangeRatesJson;
import com.enigma.superwallet.repository.CurrencyHistoryRepository;
import com.enigma.superwallet.repository.CurrencyRepository;
import com.enigma.superwallet.service.CurrencyHistoryService;
import com.enigma.superwallet.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyHistoryServiceImpl implements CurrencyHistoryService {
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Override
    public void saveCurrencyHistory(String date, String baseCurrency) {
        LocalDate localDate = LocalDate.parse(date);
        Timestamp timestamp = Timestamp.valueOf(localDate.atTime(LocalTime.MIDNIGHT));
        Long time = timestamp.getTime();

        String url = String.format("%s/history/%s/%d/%d/%d",
                apiUrl, baseCurrency, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());

        RestClient restClient = RestClient.create();

        ExchangeRatesJson result = restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ExchangeRatesJson.class);

        Map<String, Double> conversionRates = result != null ? result.getConversionRates() : null;

        for (ECurrencyCode currencyCode : ECurrencyCode.values()) {
            Currency currency = currencyService.getOrSaveCurrency(Currency.builder()
                            .code(currencyCode)
                            .name(currencyCode.name())
                            .build())
                    .orElseGet(() -> currencyRepository.save(new Currency(UUID.randomUUID().toString(), currencyCode, currencyCode.currencyName)));

            CurrencyHistory existingCurrencyHistory = currencyHistoryRepository
                    .findFirstByDateAndBaseAndCurrency(time, baseCurrency, currency);

            if (existingCurrencyHistory != null && baseCurrency.equals(currencyCode.name())) {
                existingCurrencyHistory.setRate(conversionRates.get(currencyCode.name()));
                currencyHistoryRepository.save(existingCurrencyHistory);
            } else {
                CurrencyHistory currencyHistory = new CurrencyHistory(UUID.randomUUID().toString(),time, baseCurrency, currency, conversionRates.get(currencyCode.name()));
                currencyHistoryRepository.save(currencyHistory);
            }
        }
    }

    @Override
    public List<CurrencyHistoryResponse> getCurrencyHistoryByDateAndBaseCurrency(String date, String baseCurrency) {
        LocalDate localDate = LocalDate.parse(date);
        Timestamp timestamp = Timestamp.valueOf(localDate.atTime(LocalTime.MIDNIGHT));
        Long time = timestamp.getTime();

        List<CurrencyHistory> currencyHistoryList = currencyHistoryRepository.findByDateAndBase(time, baseCurrency);

        if (currencyHistoryList.isEmpty()) {
            saveCurrencyHistory(date, baseCurrency);
            currencyHistoryList = currencyHistoryRepository.findByDateAndBase(time, baseCurrency);
        }

        return currencyHistoryList.stream()
                .map(CurrencyHistoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyHistoryResponse getCurrencyRate(String baseCurrency, String targetCurrency) {
        long time = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        Currency targetCurrencyData = null;
        for (ECurrencyCode code : ECurrencyCode.values()) {
            if (code.name().equals(targetCurrency)) {
                targetCurrencyData = Currency.builder()
                        .code(code)
                        .name(code.currencyName)
                        .build();
                break;
            }
        }
        Optional<Currency> dataTargetCurrency = currencyService.getOrSaveCurrency(targetCurrencyData);

        CurrencyHistory currencyHistory = currencyHistoryRepository.findFirstByDateAndBaseAndCurrency(time, baseCurrency, dataTargetCurrency.orElse(null));

            if (currencyHistory != null) {
            return new CurrencyHistoryResponse(currencyHistory);
        } else {
            String url = String.format("%s/pair/%s/%s", apiUrl, baseCurrency, targetCurrency);
            RestClient restClient = RestClient.create();

            ExchangeRatesJson result = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ExchangeRatesJson.class);

            BigDecimal conversionRate = null;
            long timeStamp = 0L;
            if (result != null) {
                conversionRate = result.getConversionRate();
                timeStamp = result.getTimeLastUpdateUnix();
            }

            LocalDate date = Instant.ofEpochSecond(timeStamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDateTime midnight = date.atStartOfDay();
            long midnightTimeStamp = midnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            CurrencyHistory newCurrencyHistory =
                    new CurrencyHistory(UUID.randomUUID().toString(),midnightTimeStamp, baseCurrency,
                            dataTargetCurrency.orElse(null), conversionRate.doubleValue());

            currencyHistoryRepository.save(newCurrencyHistory);

            saveCurrencyHistory(date.toString(),baseCurrency);
            return new CurrencyHistoryResponse(newCurrencyHistory);
        }
    }
}
