package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.AccountRequest;
import com.enigma.superwallet.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {
    void createAccount(String customerId);
    List<AccountResponse> getAllAccount();
    AccountResponse getById(String id);
    AccountResponse updateAccountBalance(String accountId, Double amount);
    AccountResponse updateIdrAccountBalance(String accountId,Double newBalance);

    AccountResponse getByAccountNumber(String accountNumber);
}
