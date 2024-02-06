package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.constant.ERole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class UserCredentialResponse {
    private String email;
    private ERole role;
}
