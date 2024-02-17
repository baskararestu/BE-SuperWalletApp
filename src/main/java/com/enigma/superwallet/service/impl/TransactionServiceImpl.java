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
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.*;
import com.enigma.superwallet.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final ValidationUtil util;
    private final JwtUtil jwtUtil;

    private double fee = 7000;

    @Transactional
    @Override
    public DepositResponse deposit(DepositRequest depositRequest) {
        try {
            String token = util.extractTokenFromHeader();

            String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");

            CustomerResponse customerResponse = customerService.getById(customerId);
            AccountResponse account = accountService.getById(depositRequest.getAccountId());
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            if (!account.getCustomer().getId().equals(customerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Deposit can only be made by the account owner");
            }

            String dummyBankId = depositRequest.getDummyBankId();
            double amount = depositRequest.getAmount();
            dummyBankService.reduceBalance(dummyBankId, amount);

            AccountResponse updated = accountService.updateIdrAccountBalance(depositRequest.getAccountId(), amount);

            TransactionType depositTransactionType = transactionTypeService.getOrSave(
                    TransactionType.builder().transactionType(ETransactionType.DEPOSIT).build());

            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .transactionDate(LocalDateTime.now())
                    .sourceAccount(Account.builder().id(account.getId()).build())
                    .destinationAccount(Account.builder().id(account.getId()).build())
                    .amount(depositRequest.getAmount())
                    .transactionType(depositTransactionType)
                    .fee(0.0)
                    .build();

            transactionRepositroy.saveAndFlush(transactionHistory);
            String formattedAmount = formatAmount(depositRequest.getAmount());
            String formattedNewBalance = formatAmount(updated.getBalance());

            return DepositResponse.builder()
                    .transactionId(transactionHistory.getId())
                    .customerName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName())
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
    public TransferResponse transferBetweenAccount(TransferRequest request) {
        AccountResponse sender = accountService.getByAccountNumber(request.getFromNumber());
        AccountResponse receiver = accountService.getByAccountNumber(request.getToNumber());
        if (sender == null || receiver == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Data Account not found");

        if (sender.getAccountNumber().equals(receiver.getAccountNumber()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot sending money to the same account number");

        if (sender.getBalance() < request.getAmountTransfer())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient balance");

        TransactionType transactionType = transactionTypeService.getOrSave(
                TransactionType.builder().transactionType(ETransactionType.TRANSFER).build());
        if (transactionType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction type not found");
        }
        return getTransfer(request, sender, receiver, transactionType);
    }

    @Transactional
    @Override
    public TransferResponse getTransfer(TransferRequest request, AccountResponse sender, AccountResponse receiver, TransactionType transactionType) {
        BigDecimal totalAmount;
        String formattedAmount;
        BigDecimal totalFee= BigDecimal.valueOf(0);
        if (sender.getCurrency() == receiver.getCurrency()) {
            fee = 0.0;
            Double newBalanceSender = sender.getBalance() - request.getAmountTransfer() - fee;
            accountService.updateAccountBalance(sender.getId(), newBalanceSender);
            Double newBalanceReceiver = receiver.getBalance() + request.getAmountTransfer();
            accountService.updateAccountBalance(receiver.getId(), newBalanceReceiver);

            formattedAmount = formatAmount(request.getAmountTransfer());
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .transactionDate(LocalDateTime.now())
                    .sourceAccount(Account.builder().id(sender.getId()).build())
                    .destinationAccount(Account.builder().id(receiver.getId()).build())
                    .amount(request.getAmountTransfer())
                    .transactionType(transactionType)
                    .fee(fee)
                    .build();
            transactionRepositroy.saveAndFlush(transactionHistory);

        } else {
            CurrencyHistoryResponse currency = currencyHistoryService.getCurrencyRate(sender.getCurrency().getCode().toString(), receiver.getCurrency().getCode().toString());
            Double amountTransfer = request.getAmountTransfer();
            BigDecimal amountTransferBigDecimal = BigDecimal.valueOf(amountTransfer);
            totalAmount = amountTransferBigDecimal.multiply(currency.getRate());

            Double totalAmountDouble = totalAmount.doubleValue();
            if (sender.getCurrency().getCode() != ECurrencyCode.IDR) {
                BigDecimal rateNow = currency.getRate();
                totalFee = BigDecimal.valueOf(fee).divide(rateNow, 15, RoundingMode.HALF_UP); // Divide fee by rate with specified scale and rounding mode
            } else {
                totalFee = BigDecimal.valueOf(fee);
            }
            Double newBalanceSender = sender.getBalance() - request.getAmountTransfer() - totalFee.doubleValue();
            if (newBalanceSender < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient balance");
            }
            accountService.updateAccountBalance(sender.getId(), newBalanceSender);
            Double newBalanceReceiver = receiver.getBalance() + totalAmountDouble;
            accountService.updateAccountBalance(receiver.getId(), newBalanceReceiver);

            formattedAmount = formatAmount(totalAmountDouble);
            TransactionHistory transactionHistory = TransactionHistory.builder()
                    .transactionDate(LocalDateTime.now())
                    .sourceAccount(Account.builder().id(sender.getId()).build())
                    .destinationAccount(Account.builder().id(receiver.getId()).build())
                    .amount(Double.valueOf(String.valueOf(totalAmount)))
                    .transactionType(transactionType)
                    .fee(totalFee.doubleValue())
                    .build();
            transactionRepositroy.saveAndFlush(transactionHistory);

        }
        return TransferResponse.builder()
                .from(sender.getCustomer().getFirstName() + sender.getCustomer().getLastName())
                .fromNumber(sender.getAccountNumber())
                .fromCurrency(sender.getCurrency().getCode().toString())
                .to(receiver.getCustomer().getFirstName() + receiver.getCustomer().getLastName())
                .toNumber(receiver.getAccountNumber())
                .toCurrency(receiver.getCurrency().getCode().toString())
                .totalAmount(formattedAmount)
                .fee(String.valueOf(totalFee))
                .build();
    }
}
