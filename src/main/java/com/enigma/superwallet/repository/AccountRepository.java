package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account>findAccountByCustomerIdAndPin(String userId,String Pin);
    Account findByAccountNumber(String accountNumber);
}
