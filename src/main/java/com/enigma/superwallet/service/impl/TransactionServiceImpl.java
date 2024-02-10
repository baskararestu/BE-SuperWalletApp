package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.service.AccountService;
import com.enigma.superwallet.service.TransactionsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService {

    private final AccountService accountService;

    @Transactional
    @Override
    public DepositResponse deposit(DepositRequest depositRequest) {
        return null;
    }
}
