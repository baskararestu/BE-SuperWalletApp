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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
        if (localDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot exceed current date");
        }
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
                    .findByDateAndBaseAndCurrency(time, baseCurrency, currency);

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

        LocalDate currentDate = LocalDate.now();
        Timestamp timestamp = Timestamp.valueOf(currentDate.atTime(LocalTime.MIDNIGHT));
        Long time = timestamp.getTime();

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

        CurrencyHistory currencyHistory = currencyHistoryRepository.findByDateAndBaseAndCurrency(time, baseCurrency, dataTargetCurrency.orElse(null));

        if (currencyHistory == null) {
            List<CurrencyHistoryResponse> newCurrencyHistories = getCurrencyHistoryByDateAndBaseCurrency(currentDate.toString(), baseCurrency);

            CurrencyHistoryResponse newCurrencyHistory = newCurrencyHistories.stream()
                    .filter(history -> history.getTargetCurrency().equals(targetCurrency))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Currency history not found for the target currency"));

            return newCurrencyHistory;
        } else {
            return new CurrencyHistoryResponse(currencyHistory);
        }
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Page<CurrencyHistoryResponse> getCurrencies(String fromDate, String toDate, String baseCurrency, Integer page, Integer size) {
        LocalDate fromDateParsed = null;
        LocalDate toDateParsed = null;

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            fromDateParsed = LocalDate.parse(fromDate);
            toDateParsed = LocalDate.parse(toDate);

            if (fromDateParsed.isAfter(toDateParsed)) {
                throw new IllegalArgumentException("fromDate cannot be after toDate");
            }

            if (ChronoUnit.DAYS.between(fromDateParsed, toDateParsed) > 6) {
                throw new IllegalArgumentException("The difference between fromDate and toDate must not exceed 7 days");
            }
        }

        Pageable pageable = PageRequest.of(page, size);

        List<CurrencyHistoryResponse> currencyHistoryResponses = new ArrayList<>();

        if (fromDateParsed == null && toDateParsed == null) {
            List<CurrencyHistory> allCurrencyHistories = currencyHistoryRepository.findAll();

            currencyHistoryResponses.addAll(allCurrencyHistories.stream()
                    .map(CurrencyHistoryResponse::new)
                    .collect(Collectors.toList()));
        } else {
            for (LocalDate currentDate = fromDateParsed; !currentDate.isAfter(toDateParsed); currentDate = currentDate.plusDays(1)) {
                Timestamp currentTimestamp = Timestamp.valueOf(currentDate.atStartOfDay());
                Long currentTime = currentTimestamp.getTime();

                List<ECurrencyCode> baseCurrencies;
                if (baseCurrency == null || baseCurrency.isEmpty()) {
                    baseCurrencies = Arrays.asList(ECurrencyCode.values());
                } else {
                    baseCurrencies = Collections.singletonList(ECurrencyCode.valueOf(baseCurrency.toUpperCase()));
                }

                for (ECurrencyCode baseCurrencyCode : baseCurrencies) {
                    List<CurrencyHistory> existingCurrencyHistories = currencyHistoryRepository.findByDateAndBase(currentTime, baseCurrencyCode.name());

                    if (existingCurrencyHistories.isEmpty()) {
                        saveCurrencyHistory(currentDate.toString(), baseCurrencyCode.name());
                        existingCurrencyHistories = currencyHistoryRepository.findByDateAndBase(currentTime, baseCurrencyCode.name());
                    }

                    List<CurrencyHistory> filteredHistories = existingCurrencyHistories.stream()
                            .filter(history -> Arrays.stream(ECurrencyCode.values())
                                    .anyMatch(targetCurrency -> targetCurrency.name().equals(history.getCurrency().getCode().name())))
                            .collect(Collectors.toList());

                    currencyHistoryResponses.addAll(filteredHistories.stream()
                            .map(CurrencyHistoryResponse::new)
                            .collect(Collectors.toList()));
                }
            }
        }

        int totalItems = currencyHistoryResponses.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int currentPage = Math.min(page, totalPages - 1);

        if (currentPage != page) {
            return new PageImpl<>(Collections.emptyList(), pageable, totalItems);
        }

        int startIdx = currentPage * size;
        int endIdx = Math.min(startIdx + size, totalItems);

        List<CurrencyHistoryResponse> pageCurrencyHistoryResponses = currencyHistoryResponses.subList(startIdx, endIdx);

        pageable = PageRequest.of(currentPage, size);

        return new PageImpl<>(pageCurrencyHistoryResponses, pageable, totalItems);
    }

}
