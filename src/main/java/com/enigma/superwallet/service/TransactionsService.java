package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.TransferResponse;
import com.enigma.superwallet.entity.TransactionType;

public interface TransactionsService {
    DepositResponse deposit(DepositRequest request);
    TransferResponse transferBetweenAccount(TransferRequest request);
    TransferResponse getTransfer(TransferRequest request, AccountResponse sender, AccountResponse receiver, TransactionType transactionType);
}
