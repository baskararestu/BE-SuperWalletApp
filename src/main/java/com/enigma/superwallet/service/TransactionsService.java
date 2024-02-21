package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.request.WithdrawalRequest;
import com.enigma.superwallet.dto.response.*;
import com.enigma.superwallet.entity.TransactionType;
import org.springframework.data.domain.Page;

public interface TransactionsService {
    DepositResponse deposit(DepositRequest request);
    TransferResponse transferBetweenAccount(TransferRequest request);
    TransferResponse getTransfer(TransferRequest request, AccountResponse sender, AccountResponse receiver, TransactionType transactionType);
    WithdrawalResponse withdraw(WithdrawalRequest request);
    Page<TransferHistoryResponse> getTransferHistoriesPaging(String name, Integer page, Integer size);
}
