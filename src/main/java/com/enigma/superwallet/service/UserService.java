package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.UserCredential;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends UserDetailsService {
    AppUser loadUserByUserId(String id);
    UserCredential updateUserCredential(UserCredential userCredential);
}
