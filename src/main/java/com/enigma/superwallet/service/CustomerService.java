package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    CustomerResponse createCustomer(Customer customer);
    List<CustomerResponse> getAll();
    CustomerResponse getById(String id);
    CustomerResponse update(RegisterRequest registerRequest);
    Boolean delete(String id);
    Optional<Customer> getCustomerByUserCredentialId (String userCredentialId);
}
