package com.enigma.superwallet.mapper;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.LoginAdminResponse;
import com.enigma.superwallet.dto.response.LoginResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;
import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.service.CustomerService;

public class AuthResponseMapper {
private static CustomerService customerService;

    public static RegisterResponse mapToRegisterResponse(UserCredential userCredential, AuthAdminRequest authAdminRequest) {
        return RegisterResponse.builder()
                .email(userCredential.getEmail())
                .fullName(authAdminRequest.getFullName())
                .phoneNumber(authAdminRequest.getPhoneNumber())
                .role(userCredential.getRole().getRoleName())
                .build();
    }

    public static RegisterResponse mapToRegisterCustomer(UserCredential userCredential, RegisterRequest registerRequest){
        return RegisterResponse.builder()
                .fullName(registerRequest.getFirstName() +" "+registerRequest.getLastName())
                .email(userCredential.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(userCredential.getRole().getRoleName())
                .build();
    }

    public static LoginResponse mapToLoginResponse(CustomerResponse customerResponse, ERole role, String token){

        return LoginResponse.builder()
                .email(customerResponse.getUserCredential().getEmail())
                .firstName(customerResponse.getFirstName())
                .lastName(customerResponse.getLastName())
                .token(token)
                .role(role.name())
                .build();
    }

    public static LoginAdminResponse mapToLoginAdminsResponse(AppUser appUser,String token){
        return LoginAdminResponse.builder()
                .token(token)
                .role(appUser.getRole().name())
                .build();
    }
}
