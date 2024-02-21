package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.DummyBankRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.dto.response.UserCredentialResponse;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.DummyBank;
import com.enigma.superwallet.repository.DummyBankRepository;
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.CustomerService;
import com.enigma.superwallet.service.DummyBankService;
import com.enigma.superwallet.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DummyBankServiceImpl implements DummyBankService {
    private final DummyBankRepository dummyBankRepo;
    private final CustomerService customerService;
    private final JwtUtil jwtUtil;
    private final ValidationUtil util;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DummyBankResponse createDummyBank(DummyBankRequest dummyBankRequest) {
        try {
            String token = util.extractTokenFromHeader();

            String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");
            CustomerResponse customerData = customerService.getById(customerId);

            if (customerData == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }

            if (customerData.getBankData() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer already has a bank account");
            }

            Random random = new Random();
            String randomDigits = String.format("%08d", random.nextInt(1000000000));

            String accountNumber = "1723" + randomDigits;

            DummyBank dummyBank = DummyBank.builder()
                    .cardNumber(dummyBankRequest.getCardNumber())
                    .accountNumber(accountNumber)
                    .holderName(dummyBankRequest.getHolderName())
                    .expDate(dummyBankRequest.getExpDate())
                    .cvv(dummyBankRequest.getCvv())
                    .build();

            dummyBank = dummyBankRepo.save(dummyBank);
            CustomerResponse customer = customerService.getById(customerId);
            CustomerResponse customerResponse = CustomerResponse.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .phoneNumber(customer.getPhoneNumber())
                    .address(customer.getAddress())
                    .gender(customer.getGender())
                    .birthDate(customer.getBirthDate())
                    .userCredential(UserCredentialResponse.builder()
                            .email(customer.getUserCredential().getEmail())
                            .role(customer.getUserCredential().getRole())
                            .pin(customer.getUserCredential().getPin())
                            .build())
                    .build();
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            customerService.updateDummyBankId(customerResponse.getId(), dummyBank.getId());

            return DummyBankResponse.builder()
                    .id(dummyBank.getId())
                    .holderName(dummyBank.getHolderName())
                    .cardNumber(dummyBank.getCardNumber())
                    .accountNumber(dummyBank.getAccountNumber())
                    .expDate(dummyBank.getExpDate())
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    @Override
    public DummyBankResponse getDummyBankByCustomerLoggedIn() {
        String token = util.extractTokenFromHeader();
        String customerId = jwtUtil.getUserInfoByToken(token).get("customerId");
        CustomerResponse customerData = customerService.getById(customerId);
        Optional<DummyBank> optionalDummyBank = dummyBankRepo.findById(customerData.getBankData().getId());
        if (optionalDummyBank.isPresent()) {
            DummyBank dummyBank = optionalDummyBank.get();
            return DummyBankResponse.builder()
                    .id(dummyBank.getId())
                    .holderName(dummyBank.getHolderName())
                    .cardNumber(dummyBank.getCardNumber())
                    .accountNumber(dummyBank.getAccountNumber())
                    .expDate(dummyBank.getExpDate())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dummy bank not found");
        }
    }
}
