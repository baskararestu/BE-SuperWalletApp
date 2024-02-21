package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.entity.Currency;
import com.enigma.superwallet.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AccountResponse {
    private String id;
    private Customer customer;
    private String accountNumber;
    private Currency currency;
    private Double balance;
}
