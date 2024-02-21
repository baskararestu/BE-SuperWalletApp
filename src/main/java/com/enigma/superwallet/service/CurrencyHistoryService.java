package com.enigma.superwallet.service;


import com.enigma.superwallet.dto.response.CurrencyHistoryResponse;
import com.enigma.superwallet.entity.CurrencyHistory;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CurrencyHistoryService {
    public void saveCurrencyHistory(String date, String baseCurrency);

    public List<CurrencyHistoryResponse> getCurrencyHistoryByDateAndBaseCurrency(String date, String baseCurrency);

    public CurrencyHistoryResponse getCurrencyRate(String baseCurrency, String targetCurrency);

    Page<CurrencyHistoryResponse> getCurrencies(String fromDate, String toDate, String baseCurrency, Integer page, Integer size);
}
