package com.enigma.superwallet.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ForgotPasswordRequest {
    private String email;
}
