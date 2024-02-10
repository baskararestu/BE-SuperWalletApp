package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.entity.TransactionType;
import com.enigma.superwallet.repository.TransactionTypeRepository;
import com.enigma.superwallet.service.TransactionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionTypeServiceImpl implements TransactionTypeService {
    private final TransactionTypeRepository transactionTypeRepository;

    @Override
    public TransactionType getTransactionTypeById(String id) {
        return null;
    }

    @Override
    public TransactionType getOrSave(TransactionType type) {
        Optional<TransactionType> optionalType = transactionTypeRepository.findByTransactionType(type.getTransactionType());
        if (!optionalType.isEmpty()) {
            return optionalType.get();
        }
        return transactionTypeRepository.save(type);
    }
}
