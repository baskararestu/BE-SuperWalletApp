package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.constant.ETransactionType;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.request.WithdrawalRequest;
import com.enigma.superwallet.dto.response.*;
import com.enigma.superwallet.entity.Account;
import com.enigma.superwallet.entity.TransactionHistory;
import com.enigma.superwallet.entity.TransactionType;
import com.enigma.superwallet.repository.TransactionRepositroy;
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.*;
import com.enigma.superwallet.util.ValidationUtil;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.enigma.superwallet.mapper.TransactionsMapper.*;
import static com.enigma.superwallet.util.WithdrawalCodeGenerator.generateUniqueWithdrawalCode;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionsService {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final TransactionTypeService transactionTypeService;
    private final TransactionRepositroy transactionRepositroy;
    private final CurrencyHistoryService currencyHistoryService;
    private final ValidationUtil util;
    private final JwtUtil jwtUtil;

    private double fee = 7000;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DepositResponse deposit(DepositRequest depositRequest) {
        try {
            String token = util.extractTokenFromHeader();

            String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");

            CustomerResponse dataCustomer = customerService.getById(customerId);
            String currentPin = dataCustomer.getUserCredential().getPin();
            String pin = depositRequest.getPin().toString();

            if (!currentPin.equals(pin) || depositRequest.getPin().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN");
            }

            AccountResponse account = accountService.getById(depositRequest.getAccountId());
            if (dataCustomer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            if (!account.getCustomer().getId().equals(customerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Deposit can only be made by the account owner");
            }

            double amount = depositRequest.getAmount();
            return getDeposit(depositRequest, amount, account);
        } catch (ResponseStatusException e) {
            throw e;
        }
    }
    private DepositResponse getDeposit(DepositRequest depositRequest, double amount, AccountResponse account) {
        AccountResponse updated = accountService.updateIdrAccountBalance(depositRequest.getAccountId(), amount);

        TransactionType depositTransactionType = transactionTypeService.getOrSave(
                TransactionType.builder().transactionType(ETransactionType.DEPOSIT).build());

        TransactionHistory transactionHistory =
                mapToTransactionHistory(
                        depositRequest.getAmount(), account, depositTransactionType, "", fee);

        transactionRepositroy.saveAndFlush(transactionHistory);
        String formattedAmount = formatAmount(depositRequest.getAmount());
        String formattedNewBalance = formatAmount(updated.getBalance());

        return mapToDepositResponse(transactionHistory, account, formattedAmount, formattedNewBalance);
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
        String token = util.extractTokenFromHeader();

        String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");

        CustomerResponse dataCustomer = customerService.getById(customerId);
        String currentPin = dataCustomer.getUserCredential().getPin();
        String pin = request.getPin();
        if (!currentPin.equals(pin) || request.getPin().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN");
        }
        if(!customerId.equals(sender.getCustomer().getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid process");

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
        BigDecimal totalFee = BigDecimal.valueOf(0);
        if (sender.getCurrency() == receiver.getCurrency()) {
            fee = 0.0;
            Double newBalanceSender = sender.getBalance() - request.getAmountTransfer() - fee;
            accountService.updateAccountBalance(sender.getId(), newBalanceSender);
            Double newBalanceReceiver = receiver.getBalance() + request.getAmountTransfer();
            accountService.updateAccountBalance(receiver.getId(), newBalanceReceiver);

            formattedAmount = formatAmount(request.getAmountTransfer());
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
        }

        TransactionHistory transactionHistory = mapToTransactionHistory
                (request.getAmountTransfer(), sender, receiver, transactionType, "",
                        totalFee.doubleValue());
        transactionRepositroy.saveAndFlush(transactionHistory);

        return mapToTransferResponse(sender, receiver, formattedAmount, totalFee);
    }

    @Transactional
    @Override
    public WithdrawalResponse withdraw(WithdrawalRequest request) {
        AccountResponse account = accountService.getById(request.getAccountId());
        CustomerResponse customer = customerService.getById(account.getCustomer().getId());
        String token = util.extractTokenFromHeader();

        String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");

        String currentPin = customer.getUserCredential().getPin();
        String pin = request.getPin();
        if (!currentPin.equals(pin) || request.getPin().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN");
        }
        if (account == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");

        if (account.getBalance() < request.getAmount())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient balance");

        if (!customer.getId().equals(customerId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid withdraw");

        String withdrawalCode = generateUniqueWithdrawalCode();

        Double newBalance = account.getBalance() - request.getAmount();
        accountService.updateAccountBalance(account.getId(), newBalance);

        TransactionType withdrawalTransactionType = transactionTypeService.getOrSave(
                TransactionType.builder().transactionType(ETransactionType.WITHDRAW).build());
        double totalAmount = request.getAmount();
        fee = 0.0;
        TransactionHistory transactionHistory =
                mapToTransactionHistory(
                        totalAmount, account, withdrawalTransactionType, withdrawalCode, fee);
        transactionRepositroy.saveAndFlush(transactionHistory);

        return mapToWithdrawalResponse(transactionHistory, withdrawalCode);
    }

    @Override
    public Page<TransferHistoryResponse> getTransferHistoriesPaging(String name, String type, Long fromDate, Long toDate, Integer page, Integer size) {
        Page<TransactionHistory> pageResult = transactionRepositroy.findAll(transactionSpecification(name, type, fromDate, toDate), PageRequest.of(page, size));
        return pageResult.map(this::mapToTransferHistoryResponse);
    }


    private Specification<TransactionHistory> transactionSpecification(String name, String type, Long fromDate, Long toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                String fullNamePattern = "%" + name + "%";

                Expression<String> sourceFirstName = root.get("sourceAccount").get("customer").get("firstName");
                Expression<String> sourceLastName = root.get("sourceAccount").get("customer").get("lastName");

                Expression<String> destinationFirstName = root.get("destinationAccount").get("customer").get("firstName");
                Expression<String> destinationLastName = root.get("destinationAccount").get("customer").get("lastName");

                Expression<String> sourceFullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(sourceFirstName, " "), sourceLastName
                );
                Expression<String> destinationFullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(destinationFirstName, " "), destinationLastName
                );

                Predicate sourceFullNamePredicate = criteriaBuilder.like(sourceFullName, fullNamePattern);

                Predicate destinationFullNamePredicate = criteriaBuilder.like(destinationFullName, fullNamePattern);

                predicates.add(criteriaBuilder.or(sourceFullNamePredicate, destinationFullNamePredicate));
            }


            if (type != null && !type.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType").get("type"), type));
            }
            if (fromDate != null) {
                LocalDateTime fromDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fromDate), ZoneId.systemDefault());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), fromDateTime));
            }
            if (toDate != null) {
                LocalDateTime toDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(toDate), ZoneId.systemDefault());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), toDateTime));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TransferHistoryResponse mapToTransferHistoryResponse(TransactionHistory transactionHistory) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(transactionHistory.getTransactionDate()), ZoneId.systemDefault());
        String date = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return TransferHistoryResponse.builder()
                .source(mapToTransferHistoryDetailsResponse(transactionHistory.getSourceAccount()))
                .destination(mapToTransferHistoryDetailsResponse(transactionHistory.getDestinationAccount()))
                .transactionType(transactionHistory.getTransactionType().getTransactionType().name())
                .totalAmount(transactionHistory.getAmount().toString())
                .totalFee(BigDecimal.valueOf(transactionHistory.getFee()))
                .date(date)
                .withdrawalCode(transactionHistory.getWithdrawalCode())
                .build();
    }

    private TransferHistoryDetailsResponse mapToTransferHistoryDetailsResponse(Account account) {
        if (account == null) {
            return null;
        }
        return TransferHistoryDetailsResponse.builder()
                .firstName(account.getCustomer().getFirstName())
                .lastName(account.getCustomer().getLastName())
                .accountNumber(account.getAccountNumber())
                .currencyCode(account.getCurrency().getCode().toString())
                .currencyName(account.getCurrency().getName())
                .build();
    }
}
