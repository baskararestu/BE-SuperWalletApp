package com.enigma.superwallet.service;


import com.enigma.superwallet.entity.CurrencyHistory;

import java.util.List;

public interface CurrencyHistoryService {
    public void saveCurrencyHistory(String date, String baseCurrency, String targetCurrencies);
    public List<CurrencyHistory> getCurrencyHistoryByDateAndBaseCurrency(String date, String baseCurrency);
}
