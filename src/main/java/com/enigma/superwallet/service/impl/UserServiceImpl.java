package com.enigma.superwallet.service.impl;


import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserCredentialRepository userCredentialRepository;

    @Override
    public AppUser loadUserByUserId(String id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public UserCredential updateUserCredential(UserCredential userCredential) {
        String userId = userCredential.getId();
        if (userId != null && loadUserByUserId(userId) != null) return userCredentialRepository.save(userCredential);
        return null;
    }
}
