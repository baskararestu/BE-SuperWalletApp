package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.request.LoginRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.LoginAdminResponse;
import com.enigma.superwallet.dto.response.LoginResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse registerCustomer(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse registerSuperAdmin(AuthAdminRequest authAdminRequest);
    RegisterResponse registerAdmin(AuthAdminRequest authAdminRequest);
}
