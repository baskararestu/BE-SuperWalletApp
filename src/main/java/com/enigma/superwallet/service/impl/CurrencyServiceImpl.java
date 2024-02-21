package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.repository.CurrencyRepository;
import com.enigma.superwallet.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    public Optional<Currency> getOrSaveCurrency(Currency currency) {
        return currencyRepository.findByCode(currency.getCode())
                .map(Optional::of)
                .orElseGet(() -> {
                    currency.setName(currency.getCode().currencyName);
                    return Optional.of(currencyRepository.save(currency));
                });
    }
}
