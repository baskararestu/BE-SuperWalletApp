package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.DepositRequest;
import com.enigma.superwallet.dto.request.TransferRequest;
import com.enigma.superwallet.dto.request.WithdrawalRequest;
import com.enigma.superwallet.dto.response.*;
import com.enigma.superwallet.service.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @PostMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(@RequestBody WithdrawalRequest withdrawalRequest) {
        try {
            WithdrawalResponse response = transactionsService.withdraw(withdrawalRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Successfully withdrawn from account")
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

@GetMapping
public ResponseEntity<?>getTransactionsHistory(
        @RequestParam(name = "name",required = false) String name,
        @RequestParam(name = "type",required = false) String type,
        @RequestParam(name = "fromDate",required = false) Long fromDate,
        @RequestParam(name = "toDate",required = false) Long toDate,
        @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(name = "size", required = false, defaultValue = "5") Integer size
){
        try {
            Page<TransferHistoryResponse> dataResponse = transactionsService.getTransferHistoriesPaging(name, type, fromDate, toDate, page, size);
            PagingResponse pagingResponse = new PagingResponse();

            pagingResponse.setCurrentPage(dataResponse.getNumber());
            pagingResponse.setTotalPage(dataResponse.getTotalPages());
            pagingResponse.setSize(dataResponse.getSize());
            pagingResponse.setTotalItem(dataResponse.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Successfully getting data")
                            .data(dataResponse.getContent())
                            .pagingResponse(pagingResponse)
                            .build());
        } catch (ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(ErrorResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getMessage())
                            .build());
        }
    }
}
