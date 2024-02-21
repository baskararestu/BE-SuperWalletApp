package com.enigma.superwallet.dto.response;

import com.enigma.superwallet.constant.Gender;
import com.enigma.superwallet.entity.DummyBank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CustomerResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private UserCredentialResponse userCredential;
    private String images;
    private DummyBank bankData;
}
