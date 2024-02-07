package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCredentialServiceImpl implements UserCredentialService {
    private final UserCredentialRepository userCredentialRepository;

    @Override
    public AppUser loadUserByUserId(String id) {
        UserCredential userCredential = userCredentialRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credential"));
        return AppUser.builder()
                .id(userCredential.getId())
                .email(userCredential.getEmail())
                .password(userCredential.getPassword())
                .role(userCredential.getRole().getRoleName())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredential userCredential = userCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credential"));
        return AppUser.builder()
                .id(userCredential.getId())
                .email(userCredential.getEmail())
                .password(userCredential.getPassword())
                .role(userCredential.getRole().getRoleName())
                .build();
    }
    @Override
    public UserCredential updateUserCredential(UserCredential userCredential) {
        String userId = userCredential.getId();
        if (userId != null && loadUserByUserId(userId) != null) return userCredentialRepository.save(userCredential);
        return null;
    }

    @Override
    public boolean isSuperAdminExists(ERole roleName) {
        return userCredentialRepository.existsByRole_RoleName(roleName);
    }
}