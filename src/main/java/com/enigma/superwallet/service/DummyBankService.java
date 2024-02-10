package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.DummyBankRequest;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.entity.DummyBank;

public interface DummyBankService {
    DummyBankResponse createDummyBank(DummyBankRequest dummyBankRequest);
    DummyBankResponse getDummyBank(String accountId);
}
