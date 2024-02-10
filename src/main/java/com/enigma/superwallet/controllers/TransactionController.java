package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.response.DepositResponse;
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
    public ResponseEntity<DepositResponse> deposit(@RequestBody DepositRequest depositRequest) {
        try {
            DepositResponse response = transactionsService.deposit(depositRequest);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }
}
