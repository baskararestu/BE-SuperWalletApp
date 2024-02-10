package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.TransactionType;

public interface TransactionTypeService {
    TransactionType getTransactionTypeById(String id);
    TransactionType getOrSave(TransactionType type);
}
