package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.constant.ETransactionType;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.response.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final DummyBankService dummyBankService;
    private final TransactionTypeService transactionTypeService;
    private final TransactionRepositroy transactionRepositroy;
    private final CurrencyHistoryService currencyHistoryService;

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

            String dummyBankId = depositRequest.getDummyBankId();
            double amount = depositRequest.getAmount();
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
            String formattedNewBalance = formatAmount(updated.getBalance());

            return DepositResponse.builder()
                    .transactionId(transactionHistory.getId())
                    .customerName(account.getFirstName() + " " + account.getLastName())
                    .amount(formattedAmount)
                    .currency(ECurrencyCode.IDR)
                    .accountNumber(account.getAccountNumber())
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

    @Override
    @Transactional
    public TransferResponse transferBetweenAccount(TransferRequest request) {
        AccountResponse sender = accountService.getByAccountNumber(request.getFromNumber());
        AccountResponse reciver = accountService.getByAccountNumber(request.getToNumber());
        if (sender == null || reciver == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Data Account not found");

        if (sender.getAccountNumber().equals(reciver.getAccountNumber()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot sending money to the same account number");

        if (sender.getBalance() < request.getAmountTransfer())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insuficient balance");

        if (sender.getCurrency() == reciver.getCurrency())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer across currency");

        TransactionType transactionType = transactionTypeService.getOrSave(
                TransactionType.builder().transactionType(ETransactionType.TRANSFER).build());
        if (transactionType == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction type not found");
        try {
            BigDecimal totalAmount;
            String formattedAmount;
            if (sender.getCurrency() == reciver.getCurrency()) {
                fee = 1000;
                Double newBalanceSender = sender.getBalance() - request.getAmountTransfer();
                accountService.updateAccountBalance(sender.getId(), newBalanceSender);
                Double newBalanceReciver = reciver.getBalance() + request.getAmountTransfer();
                accountService.updateAccountBalance(reciver.getId(), newBalanceReciver);

                formattedAmount = formatAmount(request.getAmountTransfer());
                TransactionHistory transactionHistory = TransactionHistory.builder()
                        .transactionDate(LocalDateTime.now())
                        .sourceAccount(Account.builder().id(sender.getId()).build())
                        .destinationAccount(Account.builder().id(reciver.getId()).build())
                        .amount(request.getAmountTransfer())
                        .transactionType(transactionType)
                        .fee(fee)
                        .build();
                transactionRepositroy.saveAndFlush(transactionHistory);

            } else {
                CurrencyHistoryResponse currency = currencyHistoryService.getCurrencyRate(sender.getCurrency().getCode().toString(), reciver.getCurrency().getCode().toString());
                Double amountTransfer = request.getAmountTransfer();
                BigDecimal amountTransferBigDecimal = BigDecimal.valueOf(amountTransfer);
                totalAmount = amountTransferBigDecimal.multiply(currency.getRate());
                fee = 1;

                Double totalAmountDouble = totalAmount.doubleValue();

                Double newBalanceSender = sender.getBalance() - request.getAmountTransfer() - fee;
                accountService.updateAccountBalance(sender.getId(), newBalanceSender);
                Double newBalanceReciver = reciver.getBalance() + totalAmountDouble;
                accountService.updateAccountBalance(reciver.getId(), newBalanceReciver);

                formattedAmount = formatAmount(totalAmountDouble);
                TransactionHistory transactionHistory = TransactionHistory.builder()
                        .transactionDate(LocalDateTime.now())
                        .sourceAccount(Account.builder().id(sender.getId()).build())
                        .destinationAccount(Account.builder().id(reciver.getId()).build())
                        .amount(Double.valueOf(String.valueOf(totalAmount)))
                        .transactionType(transactionType)
                        .fee(fee)
                        .build();
                transactionRepositroy.saveAndFlush(transactionHistory);

            }
            return TransferResponse.builder()
                    .from(sender.getFirstName() + sender.getLastName())
                    .fromNumber(sender.getAccountNumber())
                    .fromCurrency(sender.getCurrency().getCode().toString())
                    .to(reciver.getFirstName() + reciver.getLastName())
                    .toNumber(reciver.getAccountNumber())
                    .toCurrency(reciver.getCurrency().getCode().toString())
                    .totalAmount(formattedAmount)
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        }
    }
}
