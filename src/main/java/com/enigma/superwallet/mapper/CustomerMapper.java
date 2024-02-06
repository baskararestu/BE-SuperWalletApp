package com.enigma.superwallet.mapper;

import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.UserCredential;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerMapper {
    public static Customer mapToCustomer(UserCredential userCredential, RegisterRequest registerRequest){
        return Customer.builder()
                .userCredential(userCredential)
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .birthDate(LocalDate.parse(registerRequest.getBirthDate()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .isActive(true)
                .gender(registerRequest.getGender())
                .address(registerRequest.getAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
