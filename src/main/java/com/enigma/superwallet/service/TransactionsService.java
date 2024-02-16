package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.TransferResponse;

public interface TransactionsService {
    DepositResponse deposit(DepositRequest request);
    TransferResponse transferBetweenAccount(TransferRequest request);
}
