package com.enigma.superwallet.repository;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    Optional<Currency> findByCode(ECurrencyCode code);
}
