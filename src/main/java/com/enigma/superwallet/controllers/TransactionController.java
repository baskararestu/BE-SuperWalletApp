package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.dto.response.DepositResponse;
import com.enigma.superwallet.dto.response.ErrorResponse;
import com.enigma.superwallet.dto.response.TransferResponse;
import com.enigma.superwallet.service.TransactionsService;
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
@RequestMapping(AppPath.TRANS)
public class TransactionController {
    private final TransactionsService transactionsService;

    @PostMapping
    public ResponseEntity<?> deposit(@RequestBody DepositRequest depositRequest) {
        try {
            DepositResponse response = transactionsService.deposit(depositRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Successfully added balance to account")
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

    @PostMapping("/send")
    public ResponseEntity<?> transferToAccount(@RequestBody TransferRequest transferRequest){
        try {
            TransferResponse data = transactionsService.transferBetweenAccount(transferRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Successfully sending money")
                            .data(data)
                            .build());
        }catch (ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(ErrorResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getReason())
                            .build());
        }
    }
}
