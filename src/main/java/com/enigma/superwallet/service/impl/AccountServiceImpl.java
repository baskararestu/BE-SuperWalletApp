package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.entity.Account;
import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.AccountRepository;
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.AccountService;
import com.enigma.superwallet.service.CurrencyService;
import com.enigma.superwallet.service.CustomerService;
import com.enigma.superwallet.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.enigma.superwallet.util.ValidationCurrencyCode.isValidCurrencyCode;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;
    private final CustomerService customerService;
    private final JwtUtil jwtUtil;
    private final ValidationUtil util;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void createAccount(String customerId) {
        try {
            ECurrencyCode[] currencyCodes = ECurrencyCode.values();

            CustomerResponse customerResponse = customerService.getById(customerId);
            Customer customer = Customer.builder()
                    .id(customerResponse.getId())
                    .firstName(customerResponse.getFirstName())
                    .lastName(customerResponse.getLastName())
                    .phoneNumber(customerResponse.getPhoneNumber())
                    .birthDate(customerResponse.getBirthDate())
                    .gender(customerResponse.getGender())
                    .address(customerResponse.getAddress())
                    .userCredential(UserCredential.builder()
                            .email(customerResponse.getUserCredential().getEmail())
                            .build())
                    .build();

            for (ECurrencyCode currencyCode : currencyCodes) {
                createAccountForCurrency(customer, currencyCode);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to create accounts for all currencies");
        }
    }

    private void createAccountForCurrency(Customer customer, ECurrencyCode currencyCode) {
        try {
            if (!isValidCurrencyCode(currencyCode.toString())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid currency code");
            }

            Currency currency = Currency.builder()
                    .code(currencyCode)
                    .name(currencyCode.currencyName)
                    .build();

            Optional<Currency> optionalCurrency = currencyService.getOrSaveCurrency(currency);
            if (optionalCurrency.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get or save currency");
            }
            currency = optionalCurrency.get();

            // Generate a random account number
            int defaultNum = 100;
            int min = 1000000;
            int max = 9999999;
            int random = min + (int) (Math.random() * ((max - min) + 1));

            Account account = Account.builder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .currency(currency)
                    .accountNumber(String.valueOf(defaultNum) + random)
                    .balance(0d)
                    .customer(customer)
                    .build();
            accountRepository.save(account);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to create account for currency: " + currencyCode);
        }
    }

    @Override
    public List<AccountResponse> getAllAccount() {
            String token = util.extractTokenFromHeader();
            String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");

            List<Account> customerAccounts = accountRepository.findAllByCustomerId(customerId);
            if(customerAccounts==null){
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid customers");
            }
            return customerAccounts.stream().map(account -> AccountResponse.builder()
                    .id(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .balance(account.getBalance())
                    .currency(account.getCurrency())
                    .build()).toList();
    }


    @Override
    public AccountResponse getById(String id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        }
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customer(account.getCustomer())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }

    @Override
    @Transactional
    public AccountResponse updateAccountBalance(String accountId, Double amount) {
        try {
            Optional<Account> optionalAccount = accountRepository.findById(accountId);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();

                account.setBalance(amount);

                return AccountResponse.builder()
                        .id(account.getId())
                        .customer(account.getCustomer())
                        .accountNumber(account.getAccountNumber())
                        .currency(account.getCurrency())
                        .balance(account.getBalance())
                        .build();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update account balance", e);
        }
    }

    @Override
    public AccountResponse updateIdrAccountBalance(String accountId, Double newBalance) {
        try {
            Optional<Account> optionalAccount = accountRepository.findById(accountId);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                if (account.getCurrency().getCode() == ECurrencyCode.IDR) {
                    Double oldBalance = account.getBalance();
                    Double updatedBalance = oldBalance + newBalance;
                    account.setBalance(updatedBalance);
                    return AccountResponse.builder()
                            .id(account.getId())
                            .customer(account.getCustomer())
                            .accountNumber(account.getAccountNumber())
                            .currency(account.getCurrency())
                            .balance(account.getBalance())
                            .build();
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account currency is not IDR");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update account balance", e);
        }
    }

    @Override
    public AccountResponse getByAccountNumber(String accountNumber) {
       Account dataAccount= accountRepository.findByAccountNumber(accountNumber);
        if(dataAccount ==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account number not found");
        }

        return AccountResponse.builder()
                .id(dataAccount.getId())
                .customer(dataAccount.getCustomer())
                .accountNumber(dataAccount.getAccountNumber())
                .currency(dataAccount.getCurrency())
                .balance(dataAccount.getBalance())
                .build();
    }
}
