package com.enigma.superwallet.mapper;

import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityMapper {
    public static ResponseEntity<?> mapToResponseEntity(HttpStatus httpStatus, String message, Object object) {
        return ResponseEntity.status(httpStatus)
                .body(DefaultResponse.builder()
                        .statusCode(httpStatus.value())
                        .message(message)
                        .data(object)
                        .build());
    }

    public static ResponseEntity<?> mapToResponseEntity(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus)
                .body(ErrorResponse.builder()
                        .statusCode(httpStatus.value())
                        .message(message)
                        .build());
    }
}
