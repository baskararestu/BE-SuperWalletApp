package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.response.DepositResponse;

public interface TransactionsService {
    DepositResponse deposit(DepositRequest request);
}
