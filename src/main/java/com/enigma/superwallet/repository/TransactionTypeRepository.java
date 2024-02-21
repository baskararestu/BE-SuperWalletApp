package com.enigma.superwallet.repository;

import com.enigma.superwallet.constant.ETransactionType;
import com.enigma.superwallet.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType,String> {
    Optional<TransactionType> findByTransactionType(ETransactionType type);
}
