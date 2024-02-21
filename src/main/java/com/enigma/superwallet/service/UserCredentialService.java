package com.enigma.superwallet.service;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.UserCredential;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserCredentialService extends UserDetailsService {
    AppUser loadUserByUserId(String id);
    UserCredential updateUserCredential(UserCredential userCredential);
    boolean isSuperAdminExists(ERole roleName);
    UserCredential createPin(String pin);
    UserCredential changePassword(String password);
}
