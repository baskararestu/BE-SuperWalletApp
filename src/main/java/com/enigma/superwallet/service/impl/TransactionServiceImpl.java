package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.service.AccountService;
import com.enigma.superwallet.service.DummyBankService;
import com.enigma.superwallet.service.TransactionsService;
import com.enigma.superwallet.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final DummyBankService dummyBankService;

    @Transactional
    @Override
    public DepositResponse deposit(DepositRequest depositRequest) {
        try {
            // Fetch customer by ID
            CustomerResponse customerResponse = customerService.getById(depositRequest.getCustomerId());
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }

            // Deduct deposit amount from the dummy bank balance
            String dummyBankId = depositRequest.getDummyBankId(); // Assuming the deposit request contains the dummy bank ID
            double amount = depositRequest.getAmount();
            DummyBankResponse dummyBankResponse = dummyBankService.reduceBalance(dummyBankId, amount);

            // Perform the deposit operation on the default wallet associated with the customer
            AccountResponse account =    accountService.updateIdrAccountBalance(depositRequest.getAccountId(), amount);

            // Create and return the deposit response
            return DepositResponse.builder()
                    .customerId(depositRequest.getCustomerId())
                    .amount(depositRequest.getAmount())
                    .currency(ECurrencyCode.IDR)
                    .accountId(depositRequest.getAccountId())
                    .newBalance(account.getBalance())
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }
}
