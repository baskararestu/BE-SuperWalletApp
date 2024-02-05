package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.response.RegisterResponse;
import com.enigma.superwallet.entity.Admin;
import com.enigma.superwallet.entity.Role;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.RoleRepository;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.service.AdminService;
import com.enigma.superwallet.service.AuthService;
import com.enigma.superwallet.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AdminService adminService;
    private final RoleService roleService;


    @Transactional
    @Override
    public RegisterResponse registerSuperAdmin(AuthAdminRequest authAdminRequest) {
        try {
            Optional<Role> superAdminRole = roleRepository.findByRoleName(ERole.ROLE_SUPER_ADMIN);
            if (superAdminRole.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Super admin already exists");
            }
            Role role = Role.builder()
                    .roleName(ERole.ROLE_SUPER_ADMIN)
                    .build();
            role = roleService.getOrSave(role);

            UserCredential userCredential = UserCredential.builder()
                    .email(authAdminRequest.getEmail())
                    .password(passwordEncoder.encode(authAdminRequest.getPassword()))
                    .role(role)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userCredentialRepository.saveAndFlush(userCredential);
            Admin admin = Admin.builder()
                    .fullName(authAdminRequest.getFullName())
                    .userCredential(userCredential)
                    .address(authAdminRequest.getAddress())
                    .phoneNumber(authAdminRequest.getPhoneNumber())
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            adminService.createSuperAdmin(admin);
            return  RegisterResponse.builder()
                    .email(userCredential.getEmail())
                    .fullName(authAdminRequest.getFullName())
                    .phoneNumber(authAdminRequest.getPhoneNumber())
                    .role(ERole.ROLE_SUPER_ADMIN)
                    .build();

        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "user admin already exist");
        }
    }
}
