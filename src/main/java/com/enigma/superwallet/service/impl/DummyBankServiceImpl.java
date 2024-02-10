package com.enigma.superwallet.service.impl;


import com.enigma.superwallet.entity.DummyBank;
import com.enigma.superwallet.repository.DummyBankAccountRepository;
import com.enigma.superwallet.service.DummyBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DummyBankServiceImpl implements DummyBankService {
    private final DummyBankAccountRepository dummyBankRepo;

    @Override
    public DummyBank createDummyBank(String accountNumber, Double initialBalance) {
        return null;
    }

    @Override
    public DummyBank getDummyBank(String accountId) {
        return null;
    }
}
