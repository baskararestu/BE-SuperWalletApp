package com.enigma.superwallet.mapper;

import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.entity.Admin;
import com.enigma.superwallet.entity.UserCredential;

import java.time.LocalDateTime;

public class AdminMapper {

    public static Admin mapToAdminRequest(AuthAdminRequest authAdminRequest, UserCredential userCredential) {
        return Admin.builder()
                .fullName(authAdminRequest.getFullName())
                .address(authAdminRequest.getAddress())
                .phoneNumber(authAdminRequest.getPhoneNumber())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userCredential(userCredential)
                .build();
    }
}
