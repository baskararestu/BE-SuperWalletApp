package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse registerSuperAdmin(AuthAdminRequest authAdminRequest);
}
