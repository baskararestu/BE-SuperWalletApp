package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.CUSTOMER)
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllCustomer() {
        List<CustomerResponse> customerList = customerService.getAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Fetch Customer List Success")
                        .data(customerList)
                        .build());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        CustomerResponse customer = customerService.getById(id);
        if (customer != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Fetch Success")
                            .data(customer)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message("Data Not Found")
                        .build());
    }

    @PutMapping
    public ResponseEntity<?> updateCustomer(@RequestBody RegisterRequest registerRequest) {
        CustomerResponse customer = customerService.update(registerRequest);
        if (customer != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Update Success")
                            .data(customer)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message("Update Failed")
                        .build());
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> softDeleteCustomer(@PathVariable String id) {
        Boolean customer = customerService.delete(id);
        if (customer) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Delete Success")
                            .build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message("Delete Failed")
                        .build());
    }
}
