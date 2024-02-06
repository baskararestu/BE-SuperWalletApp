package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.entity.Admin;
import com.enigma.superwallet.repository.AdminRepository;
import com.enigma.superwallet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;

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
}
