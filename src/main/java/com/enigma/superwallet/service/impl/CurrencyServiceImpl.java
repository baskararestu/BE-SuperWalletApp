package com.enigma.superwallet.service.impl;

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
    public Currency getOrSaveCurrency(Currency currency) {
        Optional<Currency> optionalCode = currencyRepository.findByCode(currency.getCode());
        if(optionalCode.isPresent()) return optionalCode.get();
        return currencyRepository.save(currency);
    }
}
