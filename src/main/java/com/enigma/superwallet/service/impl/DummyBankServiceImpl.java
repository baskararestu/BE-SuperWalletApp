package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.DummyBankRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.entity.DummyBank;
import com.enigma.superwallet.repository.DummyBankRepository;
import com.enigma.superwallet.service.CustomerService;
import com.enigma.superwallet.service.DummyBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DummyBankServiceImpl implements DummyBankService {
    private final DummyBankRepository dummyBankRepo;
    private final CustomerService customerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DummyBankResponse createDummyBank(DummyBankRequest dummyBankRequest) {
        try {
            DummyBank dummyBank = DummyBank.builder()
                    .bankNumber(dummyBankRequest.getBankNumber())
                    .cvv(dummyBankRequest.getCvv())
                    .balance(dummyBankRequest.getBalance())
                    .build();

            dummyBank = dummyBankRepo.save(dummyBank);
            CustomerResponse customerResponse = customerService.getById(dummyBankRequest.getCustomerId());
            if (customerResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
            }
            customerService.updateDummyBankId(customerResponse.getId(), dummyBank.getId());
            return DummyBankResponse.builder()
                    .id(dummyBank.getId())
                    .bankNumber(dummyBank.getBankNumber())
                    .balance(dummyBank.getBalance())
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    @Override
    public DummyBankResponse getDummyBank(String accountId) {
        return null;
    }
}
