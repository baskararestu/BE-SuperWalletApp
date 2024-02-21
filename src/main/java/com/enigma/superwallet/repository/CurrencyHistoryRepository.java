package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.CurrencyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public interface CurrencyHistoryRepository extends JpaRepository<CurrencyHistory,String> {
    CurrencyHistory findByDateAndBaseAndCurrency(Long date, String base, Currency currency);
    List<CurrencyHistory> findByDateAndBase(Long date, String base);
    CurrencyHistory findByCurrencyId(String currencyId);
}
