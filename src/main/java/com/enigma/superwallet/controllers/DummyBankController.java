package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.DummyBankRequest;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.dto.response.DummyBankResponse;
import com.enigma.superwallet.service.DummyBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(AppPath.DUMMY_BANK)
@RequiredArgsConstructor
public class DummyBankController {

    private final DummyBankService dummyBankService;

    @PostMapping
    public ResponseEntity<?> createDummyBank(@RequestBody DummyBankRequest dummyBankRequest) {
        try {
            DummyBankResponse response = dummyBankService.createDummyBank(dummyBankRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Successfully create dummy bank")
                            .data(response)
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

