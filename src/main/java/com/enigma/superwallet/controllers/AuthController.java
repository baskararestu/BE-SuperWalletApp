package com.enigma.superwallet.controllers;


import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;
import com.enigma.superwallet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.AUTH)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/admins/super-admin")
    public ResponseEntity createAdminAccount(@RequestBody AuthAdminRequest authAdminRequest) {
        try {
            RegisterResponse registerResponse = authService.registerSuperAdmin(authAdminRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Successfully create admin account")
                            .data(registerResponse)
                            .build());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(DefaultResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getReason())
                            .build());
        }
    }
}
