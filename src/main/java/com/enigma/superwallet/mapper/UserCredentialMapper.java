package com.enigma.superwallet.mapper;

import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.entity.Role;
import com.enigma.superwallet.entity.UserCredential;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class UserCredentialMapper {
    private static PasswordEncoder passwordEncoder;

    public static UserCredential mapToUserCredential(AuthAdminRequest authAdminRequest, String passwordHashed, Role role) {
        return UserCredential.builder()
                .email(authAdminRequest.getEmail())
                .password(passwordHashed)
                .role(role)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserCredential mapToUserCredentialCustomer(RegisterRequest registerRequest,String passwordHashed, Role role) {
        return UserCredential.builder()
                .email(registerRequest.getEmail())
                .password(passwordHashed)
                .role(role)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
