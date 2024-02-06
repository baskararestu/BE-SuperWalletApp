package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.LoginRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.LoginResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse registerSuperAdmin(AuthAdminRequest authAdminRequest);
}
