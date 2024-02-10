package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.constant.ETransactionType;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.entity.Account;
import com.enigma.superwallet.entity.TransactionHistory;
import com.enigma.superwallet.entity.TransactionType;
import com.enigma.superwallet.repository.TransactionRepositroy;
import com.enigma.superwallet.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final DummyBankService dummyBankService;
    private final TransactionTypeService transactionTypeService;
    private final TransactionRepositroy transactionRepositroy;

    private double fee;

    @Transactional
    @Override
    public DepositResponse deposit(DepositRequest depositRequest) {
        try {
            fee = 1000;
            CustomerResponse customerResponse = customerService.getById(depositRequest.getCustomerId());
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }

            // Deduct deposit amount from the dummy bank balance
            String dummyBankId = depositRequest.getDummyBankId();
            double amount = depositRequest.getAmount() + fee;
            dummyBankService.reduceBalance(dummyBankId, amount);

            AccountResponse updated = accountService.updateIdrAccountBalance(depositRequest.getAccountId(), amount);

            TransactionType depositTransactionType = transactionTypeService.getOrSave(
                    TransactionType.builder().transactionType(ETransactionType.DEPOSIT).build());

            AccountResponse account = accountService.getById(depositRequest.getAccountId());

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .transactionDate(LocalDateTime.now())
                    .sourceAccount(Account.builder().id(account.getId()).build())
                    .destinationAccount(Account.builder().id(account.getId()).build())
                    .amount(depositRequest.getAmount())
                    .transactionType(depositTransactionType)
                    .fee(fee)
                    .build();

            transactionRepositroy.saveAndFlush(transactionHistory);
            String formattedAmount = formatAmount(depositRequest.getAmount());
            String formattedNewBalance = formatAmount(account.getBalance());

            return DepositResponse.builder()
                    .customerId(depositRequest.getCustomerId())
                    .amount(formattedAmount)
                    .currency(ECurrencyCode.IDR)
                    .accountId(depositRequest.getAccountId())
                    .newBalance(formattedNewBalance)
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    private String formatAmount(Double amount) {
        if (amount % 1 == 0) {
            return String.format("%.0f", amount);
        } else {
            return String.format("%.2f", amount);
        }
    }
}
