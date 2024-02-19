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

            String customerId = jwtUtil.getUserInfoByToken(token).get("userId");
            System.out.println(customerId);

            Double dummyBalance = 10000000.0;
            DummyBank dummyBank = DummyBank.builder()
                    .bankNumber(dummyBankRequest.getBankNumber())
                    .cvv(dummyBankRequest.getCvv())
                    .balance(dummyBalance)
                    .build();

            dummyBank = dummyBankRepo.save(dummyBank);
            Optional<Customer> customer = customerService.getCustomerByUserCredentialId(customerId);
            CustomerResponse customerResponse = CustomerResponse.builder()
                    .id(customer.get().getId())
                    .firstName(customer.get().getFirstName())
                    .lastName(customer.get().getLastName())
                    .phoneNumber(customer.get().getPhoneNumber())
                    .address(customer.get().getAddress())
                    .gender(customer.get().getGender())
                    .birthDate(customer.get().getBirthDate())
                    .userCredential(UserCredentialResponse.builder()
                            .email(customer.get().getUserCredential().getEmail())
                            .role(customer.get().getUserCredential().getRole().getRoleName())
                            .build())
                    .build();
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            customerService.updateDummyBankId(customerResponse.getId(), dummyBank.getId());
            String formattedBalance = String.format("%.2f", dummyBank.getBalance());

            return DummyBankResponse.builder()
                    .id(dummyBank.getId())
                    .bankNumber(dummyBank.getBankNumber())
                    .balance(formattedBalance)
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    @Override
    public DummyBankResponse getDummyBankById(String id) {
        Optional<DummyBank> optionalDummyBank = dummyBankRepo.findById(id);
        if (optionalDummyBank.isPresent()) {
            DummyBank dummyBank = optionalDummyBank.get();
            String formattedBalance = String.format("%.2f", dummyBank.getBalance());
            return DummyBankResponse.builder()
                    .id(dummyBank.getId())
                    .bankNumber(dummyBank.getBankNumber())
                    .balance(formattedBalance)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dummy bank not found");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DummyBankResponse reduceBalance(String id, double amount) {
        // Retrieve the DummyBank entity
        Optional<DummyBank> optionalDummyBank = dummyBankRepo.findById(id);
        if (optionalDummyBank.isPresent()) {
            DummyBank dummyBank = optionalDummyBank.get();

            if (dummyBank.getBalance() >= amount) {
                dummyBank.setBalance(dummyBank.getBalance() - amount);

                dummyBank = dummyBankRepo.save(dummyBank);

                String formattedBalance = String.format("%.2f", dummyBank.getBalance());

                return DummyBankResponse.builder()
                        .id(dummyBank.getId())
                        .bankNumber(dummyBank.getBankNumber())
                        .balance(formattedBalance)
                        .build();
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dummy bank not found");
        }
    }
}
