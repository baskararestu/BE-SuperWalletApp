package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepositroy extends JpaRepository<TransactionHistory,String>, JpaSpecificationExecutor<TransactionHistory> {
}
