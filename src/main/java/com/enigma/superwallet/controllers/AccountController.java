package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.AccountRequest;
import com.enigma.superwallet.dto.response.AccountResponse;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.dto.response.ErrorResponse;
import com.enigma.superwallet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.ACCOUNT)
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<?> getAllAccount() {
        try {
            List<AccountResponse> accountList = accountService.getAllAccount();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Fetch Success")
                            .data(accountList)
                            .build());
        }catch (ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(ErrorResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getReason())
                            .build());
        }

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
