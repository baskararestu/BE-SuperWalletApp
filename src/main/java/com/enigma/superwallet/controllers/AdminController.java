package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.AdminRequest;
import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.dto.response.DefaultResponse;
import com.enigma.superwallet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping(AppPath.ADMIN)
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAllAdmin() {
        List<AdminResponse> adminList = adminService.getAllAdmin();
        return ResponseEntity.status(HttpStatus.OK)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Fetch Admin List Success")
                        .data(adminList)
                        .build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateAdmin(@RequestBody AdminRequest adminRequest) {
        AdminResponse adminResponse = adminService.updateAdmin(adminRequest);
        if (adminResponse != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Update Admin Success")
                            .data(adminResponse)
                            .build());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message("Update Failed")
                        .build());
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> softDeleteAdmin(@PathVariable String id) {
        Boolean admin = adminService.softDeleteAdmin(id);
        if (admin) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(DefaultResponse.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Delete Admin Success")
                            .build());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DefaultResponse.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message("Delete Failed")
                        .build());
    }
}
