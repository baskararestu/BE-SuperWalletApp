package com.enigma.superwallet.dto.request;

import com.enigma.superwallet.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RegisterRequest {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String birthDate;
    private Gender gender;
    private String address;
    private String email;
    private String password;
}
