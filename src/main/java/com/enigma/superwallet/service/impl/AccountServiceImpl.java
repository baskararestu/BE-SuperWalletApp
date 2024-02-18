package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ECurrencyCode;
import com.enigma.superwallet.dto.request.AccountRequest;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.entity.Account;
import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.AccountRepository;
import com.enigma.superwallet.service.AccountService;
import com.enigma.superwallet.service.CurrencyService;
import com.enigma.superwallet.service.CustomerService;
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

    @Transactional(rollbackOn = Exception.class)
    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {
        System.out.println(accountRequest);
        try {
            if (!isValidCurrencyCode(accountRequest.getCurrencyCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid currency code");
            }

            ECurrencyCode defaultCode = ECurrencyCode.valueOf(accountRequest.getCurrencyCode());

            Currency currency = Currency.builder()
                    .code(defaultCode)
                    .name(defaultCode.currencyName)
                    .build();

            Optional<Currency> optionalCurrency = currencyService.getOrSaveCurrency(currency);
            if (optionalCurrency.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get or save currency");
            }
            currency = optionalCurrency.get();

            int defaultNum = 100;
            int min = 1000000;
            int max = 9999999;
            int random = min + (int) (Math.random() * ((max - min) + 1));

            CustomerResponse customerResponse = customerService.getById(accountRequest.getCustomerId());
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

            Account account = Account.builder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .currency(currency)
                    .accountNumber(String.valueOf(defaultNum + random))
                    .balance(0d)
                    .customer(customer)
                    .build();
            accountRepository.save(account);
            return AccountResponse.builder()
                    .id(account.getId())
                    .customer(account.getCustomer())
                    .accountNumber(account.getAccountNumber())
                    .currency(account.getCurrency())
                    .balance(account.getBalance())
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account Creation Failed");
        }
    }

    @Override
    public List<AccountResponse> getAllAccount() {
        return accountRepository.findAll().stream().map(account -> AccountResponse
                .builder()
                .id(account.getId())
                .customer(account.getCustomer())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build()).toList();
    }

    @Override
    public AccountResponse getById(String id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account != null) {
            return AccountResponse.builder()
                    .id(account.getId())
//                    .firstName(account.getCustomer().getFirstName())
//                    .lastName(account.getCustomer().getLastName())
                    .customer(account.getCustomer())
                    .accountNumber(account.getAccountNumber())
                    .currency(account.getCurrency())
                    .balance(account.getBalance())
                    .build();
        }
        return null;
    }

    @Override
    @Transactional
    public AccountResponse createDefaultAccount(String customerId) {
        try {
            ECurrencyCode defaultCode = ECurrencyCode.IDR;
            Currency currency = Currency.builder()
                    .code(defaultCode)
                    .name(defaultCode.currencyName)
                    .build();
            Optional<Currency> optionalCurrency = currencyService.getOrSaveCurrency(currency);
            if (optionalCurrency.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get or save currency");
            }
            currency = optionalCurrency.get();
            int defaultNum = 100;
            int min = 1000000;
            int max = 9999999;
            int random = min + (int) (Math.random() * ((max - min) + 1));

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

            Account account = Account.builder()
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .currency(currency)
                    .accountNumber(String.valueOf(defaultNum) + random)
                    .balance(0d)
                    .customer(customer)
                    .build();
            accountRepository.save(account);
            return AccountResponse.builder()
                    .id(account.getId())
//                    .firstName(account.getCustomer().getFirstName())
//                    .lastName(account.getCustomer().getLastName())
                    .customer(account.getCustomer())
                    .accountNumber(account.getAccountNumber())
                    .currency(account.getCurrency())
                    .balance(account.getBalance())
                    .build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account Creation Failed");
        }
    }

    @Override
    public AccountResponse findAccountByCustomerIdAndPin(String userId, String pin) {
        return accountRepository.findAccountByCustomerIdAndPin(userId, pin)
                .map(account -> AccountResponse.builder()
                        .id(account.getId())
//                        .firstName(account.getCustomer().getFirstName())
//                        .lastName(account.getCustomer().getLastName())
                        .customer(account.getCustomer())
                        .accountNumber(account.getAccountNumber())
                        .currency(account.getCurrency())
                        .balance(account.getBalance())
                        .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or pin is incorrect"));
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
//                        .firstName(account.getCustomer().getFirstName())
//                        .lastName(account.getCustomer().getLastName())
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
//                            .firstName(account.getCustomer().getFirstName())
//                            .lastName(account.getCustomer().getLastName())
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
//                .firstName(dataAccount.getCustomer().getFirstName())
//                .lastName(dataAccount.getCustomer().getLastName())
                .customer(dataAccount.getCustomer())
                .accountNumber(dataAccount.getAccountNumber())
                .currency(dataAccount.getCurrency())
                .balance(dataAccount.getBalance())
                .build();
    }
}
