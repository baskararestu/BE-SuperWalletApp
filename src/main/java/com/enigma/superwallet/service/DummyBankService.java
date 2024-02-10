package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.DummyBank;

public interface DummyBankService {
    DummyBank createDummyBank(String accountNumber, Double initialBalance);
    DummyBank getDummyBank(String accountId);
}
