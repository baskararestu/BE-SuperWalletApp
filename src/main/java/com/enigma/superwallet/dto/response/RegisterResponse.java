package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.constant.ERole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RegisterResponse {
    private String fullName;
    private String email;
    private String phoneNumber;
    private ERole role;
}
