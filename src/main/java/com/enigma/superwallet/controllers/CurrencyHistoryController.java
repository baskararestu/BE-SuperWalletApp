package com.enigma.superwallet.controllers;

import com.enigma.superwallet.entity.CurrencyHistory;
import com.enigma.superwallet.service.CurrencyHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyHistoryController {
    private final CurrencyHistoryService currencyHistoryService;

    @PostMapping("/api/currency/save")
    public ResponseEntity<String> saveCurrencyHistory(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("baseCurrency") String baseCurrency,
            @RequestParam("targetCurrencies") String targetCurrencies
    ) {
        currencyHistoryService.saveCurrencyHistory(date.toString(), baseCurrency, targetCurrencies);
        return ResponseEntity.status(HttpStatus.CREATED).body("Currency history saved successfully");
    }

    @PostMapping("/api/currency/get")
    public ResponseEntity<List<CurrencyHistory>> getCurrencyHistory(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("baseCurrency") String baseCurrency
    ) {
        List<CurrencyHistory> currencyHistoryList = currencyHistoryService.getCurrencyHistoryByDateAndBaseCurrency(date.toString(), baseCurrency);
        return ResponseEntity.ok(currencyHistoryList);
    }
}
