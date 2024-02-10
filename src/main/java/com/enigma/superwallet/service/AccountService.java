package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.AccountRequest;
import com.enigma.superwallet.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountRequest accountRequest);
    List<AccountResponse> getAllAccount();
    AccountResponse getById(String id);
    AccountResponse createDefaultAccount(String customerId);
    AccountResponse findAccountByCustomerIdAndPin(String userId, String pin);
    AccountResponse updateAccountBalance(String accountId, Double newBalance);
    AccountResponse updateIdrAccountBalance(String accountId,Double newBalance);
}
