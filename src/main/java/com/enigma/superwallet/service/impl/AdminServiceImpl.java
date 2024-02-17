package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.AdminRequest;
import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.entity.Admin;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.AdminRepository;
import com.enigma.superwallet.service.AdminService;
import com.enigma.superwallet.service.UserCredentialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final UserCredentialService userCredentialService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminResponse createSuperAdmin(Admin admin) {
        adminRepository.saveAndFlush(admin);
        return AdminResponse.builder()
                .id(admin.getId())
                .email(admin.getUserCredential().getEmail())
                .fullName(admin.getFullName())
                .phoneNumber(admin.getPhoneNumber())
                .role(admin.getUserCredential().getRole().getRoleName())
                .build();
    }

    @Override
    public List<AdminResponse> getAllAdmin() {
        return adminRepository.findAll()
                .stream()
                .filter(Admin::getIsActive)
                .map(admin -> AdminResponse.builder()
                        .id(admin.getId())
                        .fullName(admin.getFullName())
                        .email(admin.getUserCredential().getEmail())
                        .phoneNumber(admin.getPhoneNumber())
                        .role(admin.getUserCredential().getRole().getRoleName())
                        .build())
                .toList();
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public AdminResponse updateAdmin(AdminRequest adminRequest) {
        Admin admin = adminRepository.findById(adminRequest.getId()).orElse(null);
        UserCredential userCredential;
        if (admin != null) {
            if (adminRequest.getEmail() != null || adminRequest.getPassword() != null) {
                userCredential = UserCredential.builder()
                        .id(admin.getUserCredential().getId())
                        .createdAt(admin.getCreatedAt())
                        .updatedAt(LocalDateTime.now())
                        .email(adminRequest.getEmail())
                        .password(passwordEncoder.encode(adminRequest.getPassword()))
                        .role(admin.getUserCredential().getRole())
                        .build();
            }
            else {
                userCredential = UserCredential.builder()
                        .id(admin.getUserCredential().getId())
                        .createdAt(admin.getCreatedAt())
                        .updatedAt(admin.getUpdatedAt())
                        .email(adminRequest.getEmail())
                        .password(passwordEncoder.encode(adminRequest.getPassword()))
                        .role(admin.getUserCredential().getRole())
                        .build();
            }
            userCredentialService.updateUserCredential(userCredential);
            Admin updatedAdmin = Admin.builder()
                    .id(adminRequest.getId())
                    .createdAt(admin.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .fullName(adminRequest.getFullName())
                    .address(adminRequest.getAddress())
                    .phoneNumber(adminRequest.getPhoneNumber())
                    .isActive(admin.getIsActive())
                    .userCredential(userCredential)
                    .build();
            adminRepository.save(updatedAdmin);
            return AdminResponse.builder()
                    .id(updatedAdmin.getId())
                    .fullName(updatedAdmin.getFullName())
                    .email(updatedAdmin.getUserCredential().getEmail())
                    .phoneNumber(updatedAdmin.getPhoneNumber())
                    .role(updatedAdmin.getUserCredential().getRole().getRoleName())
                    .build();
        }
        return null;
    }

    @Override
    public Boolean softDeleteAdmin(String id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            Admin toDeleteAdmin = Admin.builder()
                    .id(admin.getId())
                    .createdAt(admin.getCreatedAt())
                    .updatedAt(admin.getUpdatedAt())
                    .fullName(admin.getFullName())
                    .address(admin.getAddress())
                    .phoneNumber(admin.getPhoneNumber())
                    .isActive(false)
                    .userCredential(admin.getUserCredential())
                    .build();
            adminRepository.save(toDeleteAdmin);
            return true;
        }
        return false;
    }

}
