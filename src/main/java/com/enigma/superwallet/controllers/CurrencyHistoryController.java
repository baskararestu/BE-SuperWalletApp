package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.response.*;
import com.enigma.superwallet.service.CurrencyHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.CURRENCY)
public class CurrencyHistoryController {
    private final CurrencyHistoryService currencyHistoryService;

    @PostMapping("/today")
    public ResponseEntity<?> getCurrencyHistory(
            @RequestParam("baseCurrency") String baseCurrency
    ) {
        try {
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ISO_DATE);
            List<CurrencyHistoryResponse> currencyHistoryList = currencyHistoryService.getCurrencyHistoryByDateAndBaseCurrency(formattedDate, baseCurrency);
            return ResponseEntity.status(HttpStatus.OK).body(DefaultResponse.builder()
                    .message("Success get history currency rate")
                    .statusCode(HttpStatus.OK.value())
                    .data(currencyHistoryList)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ErrorResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRateHistory(@RequestParam String baseCurrency, @RequestParam String targetCurrency) {
        try {
            CurrencyHistoryResponse result = currencyHistoryService.getCurrencyRate(baseCurrency, targetCurrency);

            return ResponseEntity.status(HttpStatus.OK).body(DefaultResponse.builder()
                    .message("Success get history currency rate")
                    .statusCode(HttpStatus.OK.value())
                    .data(result)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ErrorResponse.builder()
                            .statusCode(e.getStatusCode().value())
                            .message(e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getCurrencies(@RequestParam String fromDate, @RequestParam String toDate,
                                           @RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam String baseCurrency) {
        try {
            Page<CurrencyHistoryResponse> data = currencyHistoryService.getCurrencies(fromDate, toDate, baseCurrency, page, size);

            PagingResponse pagingResponse = new PagingResponse();

            pagingResponse.setCurrentPage(data.getNumber());
            pagingResponse.setTotalPage(data.getTotalPages());
            pagingResponse.setSize(data.getSize());
            pagingResponse.setTotalItem(data.getTotalElements());

            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.builder()
                    .message("Success get currency history")
                    .statusCode(HttpStatus.OK.value())
                    .data(data.getContent())
                    .pagingResponse(pagingResponse)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal server error")
                            .build());
        }
    }
}
