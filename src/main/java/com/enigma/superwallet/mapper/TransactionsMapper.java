package com.enigma.superwallet.mapper;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.TransferResponse;
import com.enigma.superwallet.dto.response.WithdrawalResponse;
import com.enigma.superwallet.entity.Account;
import com.enigma.superwallet.entity.TransactionHistory;
import com.enigma.superwallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionsMapper {

    public static TransactionHistory mapToTransactionHistory(double amount, AccountResponse sender, AccountResponse receiver, TransactionType type, String withdrawalCode, double fee) {
        return TransactionHistory.builder()
                .transactionDate(LocalDateTime.now())
                .sourceAccount(mapToAccount(sender))
                .destinationAccount(mapToAccount(receiver))
                .amount(amount)
                .transactionType(type)
                .fee(fee)
                .withdrawalCode(withdrawalCode)
                .build();
    }

    public static TransactionHistory mapToTransactionHistory(double amount, AccountResponse account, TransactionType type, String withdrawalCode, double fee) {
        return TransactionHistory.builder()
                .transactionDate(LocalDateTime.now())
                .sourceAccount(mapToAccount(account))
                .destinationAccount(mapToAccount(account))
                .amount(amount)
                .transactionType(type)
                .fee(fee)
                .withdrawalCode(withdrawalCode)
                .build();
    }

    private static Account mapToAccount(AccountResponse accountResponse) {
        return Account.builder()
                .id(accountResponse.getId())
                .build();
    }

    public static TransferResponse mapToTransferResponse
            (AccountResponse sender, AccountResponse receiver, String formattedAmount, BigDecimal totalFee){
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
    public static DepositResponse mapToDepositResponse(TransactionHistory transactionHistory, AccountResponse account, String formattedAmount, String formattedNewBalance) {
        return DepositResponse.builder()
                .transactionId(transactionHistory.getId())
                .customerName(account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName())
                .amount(formattedAmount)
                .currency(ECurrencyCode.IDR)
                .accountNumber(account.getAccountNumber())
                .newBalance(formattedNewBalance)
                .build();
    }

    public static WithdrawalResponse mapToWithdrawalResponse(TransactionHistory transactionHistory, String withdrawalCode) {
        return WithdrawalResponse.builder()
                .transactionId(transactionHistory.getId())
                .withdrawalCode(withdrawalCode)
                .totalAmount(transactionHistory.getAmount().toString())
                .build();
    }
}
