package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.response.CurrencyHistoryResponse;
import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.CurrencyHistory;
import com.enigma.superwallet.entity.ExchangeRatesJson;
import com.enigma.superwallet.repository.CurrencyHistoryRepository;
import com.enigma.superwallet.service.CurrencyHistoryService;
import com.enigma.superwallet.service.CurrencyService;
import com.enigma.superwallet.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyHistoryServiceImpl implements CurrencyHistoryService {
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final CurrencyService currencyService;
    private final ValidationUtil validationUtil;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Override
    public void saveCurrencyHistory(String date, String baseCurrency) {
        if (!validationUtil.isValidDate(date)) {
            throw new IllegalArgumentException("Invalid date format");
        }
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
                    .orElseThrow(() -> new IllegalStateException("Currency not found"));

            CurrencyHistory existingCurrencyHistory = currencyHistoryRepository
                    .findFirstByDateAndBaseAndCurrency(time, baseCurrency, currency);

            if (existingCurrencyHistory != null && baseCurrency.equals(currencyCode.name())) {
                existingCurrencyHistory.setRate(Objects.requireNonNull(conversionRates).get(currencyCode.name()));
                currencyHistoryRepository.save(existingCurrencyHistory);
            } else {
                CurrencyHistory currencyHistory = new CurrencyHistory(UUID.randomUUID().toString(), time, baseCurrency, currency, Objects.requireNonNull(conversionRates).get(currencyCode.name()));
                currencyHistoryRepository.save(currencyHistory);
            }
        }
    }

    @Override
    public List<CurrencyHistoryResponse> getCurrencyHistoryByDateAndBaseCurrency(String date, String baseCurrency) {
        LocalDate localDate = LocalDate.parse(date);
        Timestamp timestamp = Timestamp.valueOf(localDate.atTime(LocalTime.MIDNIGHT));
        Long time = timestamp.getTime();
        if (validationUtil.isValidCode(baseCurrency)) {
            throw new IllegalArgumentException("Invalid base currency code");
        }
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
        if (validationUtil.isValidCode(baseCurrency)) {
            throw new IllegalArgumentException("Invalid base currency code");
        }
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
            LocalDate date = Instant.ofEpochMilli(time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            saveCurrencyHistory(date.toString(), baseCurrency);

            CurrencyHistory data =
                    currencyHistoryRepository.findFirstByDateAndBaseAndCurrency
                            (time, baseCurrency, dataTargetCurrency.orElse(null));

            return new CurrencyHistoryResponse(data);
        }
    }
}
