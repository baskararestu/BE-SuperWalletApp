package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.AccountRequest;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.ACCOUNT)
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequest accountRequest) {
        System.out.println("from controller= " + accountRequest);
        AccountResponse accountResponse = accountService.createAccount(accountRequest);
        if (accountResponse != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Successfully Created New Account")
                            .data(accountResponse)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message("Failed to Create New Account")
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllAccount() {
        List<AccountResponse> accountList = accountService.getAllAccount();
        return ResponseEntity.status(HttpStatus.OK)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Fetch Success")
                        .data(accountList)
                        .build());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable String id) {
        AccountResponse accountResponse = accountService.getById(id);
        if (accountResponse != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Fetch Success")
                            .data(accountResponse)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message("Data Not Found")
                        .build());
    }
}
