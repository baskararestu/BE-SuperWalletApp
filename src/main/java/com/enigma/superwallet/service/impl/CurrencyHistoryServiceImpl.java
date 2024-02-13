package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyHistoryServiceImpl implements CurrencyHistoryService {
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Override
    public void saveCurrencyHistory(String date, String baseCurrency, String targetCurrencies) {
        LocalDate localDate = LocalDate.parse(date);

        String url = String.format("%s/history/%s/%d/%d/%d",
                apiUrl, baseCurrency, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());

        RestClient restClient = RestClient.create();

        ExchangeRatesJson result = restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ExchangeRatesJson.class);

        Map<String, Double> conversionRates = null;
        if (result != null) {
            conversionRates = result.getConversionRates();
        }

        for (ECurrencyCode currencyCode : ECurrencyCode.values()) {
            Currency currency = currencyService.getOrSaveCurrency(Currency.builder()
                            .code(currencyCode)
                            .name(currencyCode.name())
                            .build())
                    .orElseGet(() -> {
                        Currency newCurrency = new Currency();
                        newCurrency.setId(UUID.randomUUID().toString());
                        newCurrency.setCode(currencyCode);
                        newCurrency.setName(currencyCode.currencyName);
                        return currencyRepository.save(newCurrency);
                    });

            CurrencyHistory existingCurrencyHistory = currencyHistoryRepository
                    .findFirstByDateAndBaseAndCurrency(localDate, baseCurrency, currency);

            if (existingCurrencyHistory != null) {
                if (baseCurrency.equals(currencyCode.name())) {
                    existingCurrencyHistory.setRate(conversionRates.get(currencyCode.name()));
                    currencyHistoryRepository.save(existingCurrencyHistory);
                }
            } else {
                CurrencyHistory currencyHistory = new CurrencyHistory();
                currencyHistory.setDate(localDate);
                currencyHistory.setBase(baseCurrency);
                currencyHistory.setCurrency(currency);
                currencyHistory.setRate(conversionRates.get(currencyCode.name()));

                currencyHistoryRepository.save(currencyHistory);
            }
        }
    }
    @Override
    public List<CurrencyHistory> getCurrencyHistoryByDateAndBaseCurrency(String date, String baseCurrency) {
        LocalDate localDate = LocalDate.parse(date);

        // Check if currency history exists for the given date and base currency
        List<CurrencyHistory> currencyHistoryList = currencyHistoryRepository.findByDateAndBase(localDate, baseCurrency);

        // If currency history does not exist, fetch and save it from the API
        if (currencyHistoryList.isEmpty()) {
            saveCurrencyHistory(date, baseCurrency, ""); // Assuming empty string for target currencies
            currencyHistoryList = currencyHistoryRepository.findByDateAndBase(localDate, baseCurrency);
        }

        return currencyHistoryList;
    }
}
