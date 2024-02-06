package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.Currency;

public interface CurrencyService {
    Currency getOrSaveCurrency(Currency currency);
}
