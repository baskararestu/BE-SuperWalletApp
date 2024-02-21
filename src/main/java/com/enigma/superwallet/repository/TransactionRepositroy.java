package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepositroy extends JpaRepository<TransactionHistory,String>, JpaSpecificationExecutor<TransactionHistory> {
    List<TransactionHistory> findBySourceAccount_Customer_Id(String customerId);
}
