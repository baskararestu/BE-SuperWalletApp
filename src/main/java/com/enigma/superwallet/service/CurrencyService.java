package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.Currency;

import java.util.Optional;

public interface CurrencyService {
    Optional<Currency> getOrSaveCurrency(Currency currency);
}
