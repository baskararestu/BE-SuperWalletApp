package com.enigma.superwallet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AuthAdminRequest {
    private String email;
    private String password;
    private String fullName;
    private String address;
    private String phoneNumber;
}
