package com.enigma.superwallet.controllers;


import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.*;
import com.enigma.superwallet.dto.response.*;
import com.enigma.superwallet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;

import static com.enigma.superwallet.mapper.ResponseEntityMapper.mapToResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.AUTH)
public class AuthController {
    private final AuthService authService;
    private String message;

    @PostMapping("/admins/super-admin")
    public ResponseEntity<?> createAdminAccount(@RequestBody AuthAdminRequest authAdminRequest) {
        try {
            RegisterResponse data = authService.registerSuperAdmin(authAdminRequest);
            message = "Successfully create admin account";
            return mapToResponseEntity(HttpStatus.CREATED, message, data);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse data = authService.registerCustomer(registerRequest);
            message = "Register successfully";
            return mapToResponseEntity(HttpStatus.CREATED, message, data);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse data = authService.login(loginRequest);
            message = "Login successfully";
            return mapToResponseEntity(HttpStatus.OK, message, data);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    @PostMapping("/admins")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody AuthAdminRequest authAdminRequest) {
        try {
            RegisterResponse data = authService.registerAdmin(authAdminRequest);
            message = "Successfully create admin account";
            return mapToResponseEntity(HttpStatus.CREATED, message, data);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    @PostMapping("/pin")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> createPinCustomer(@RequestBody PinRequest pinRequest) {
        try {
            authService.registerPin(pinRequest);
            message = "successfully create pin";
            return mapToResponseEntity(HttpStatus.CREATED, message);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest requset) {
        try {
            authService.changePassword(requset);
            message = "successfully changed password";
            return mapToResponseEntity(HttpStatus.OK, message);
        } catch (ResponseStatusException e) {
            return mapToResponseEntity((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }
}
