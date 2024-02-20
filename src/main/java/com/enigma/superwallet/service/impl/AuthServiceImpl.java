package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.dto.request.AuthAdminRequest;
import com.enigma.superwallet.dto.request.LoginRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.LoginResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;
import com.enigma.superwallet.entity.*;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.*;
import com.enigma.superwallet.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.enigma.superwallet.mapper.UserCredentialMapper.*;
import static com.enigma.superwallet.mapper.AuthResponseMapper.*;
import static com.enigma.superwallet.mapper.AdminMapper.*;
import static com.enigma.superwallet.mapper.CustomerMapper.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserCredentialRepository userCredentialRepository;
    private final UserCredentialService userCredentialService;
    private final AdminService adminService;
    private final RoleService roleService;
    private final CustomerService customerService;
    private final AuthenticationManager authenticationManager;
    private final ValidationUtil validationUtil;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    private Authentication getAuthentication(LoginRequest loginRequest) {
        validationUtil.validate(loginRequest);
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail().toLowerCase(), loginRequest.getPassword()));
    }

    private String getEncryptPassword(AuthAdminRequest authAdminRequest) {
        return passwordEncoder.encode(authAdminRequest.getPassword());
    }

    private Role getRole(ERole roleName) {
        Role role = Role.builder()
                .roleName(roleName)
                .build();
        role = roleService.getOrSave(role);
        return role;
    }

    @Transactional
    @Override
    public RegisterResponse registerSuperAdmin(AuthAdminRequest authAdminRequest) {
        try {
            Role role = getRole(ERole.ROLE_SUPER_ADMIN);

            if (userCredentialService.isSuperAdminExists(role.getRoleName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "user admin already exist");
            }

            String passwordHashed = getEncryptPassword(authAdminRequest);

            UserCredential userCredential = mapToUserCredential(authAdminRequest, passwordHashed, role);
            userCredentialRepository.saveAndFlush(userCredential);

            Admin admin = mapToAdminRequest(authAdminRequest, userCredential);
            adminService.createSuperAdmin(admin);
            return mapToRegisterResponse(userCredential, authAdminRequest);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public RegisterResponse registerCustomer(RegisterRequest registerRequest) {
        try {
            validationUtil.validate(registerRequest);
            Role role = getRole(ERole.ROLE_CUSTOMER);

            String passwordHashed = passwordEncoder.encode(registerRequest.getPassword());
            UserCredential userCredential = mapToUserCredentialCustomer(registerRequest, passwordHashed, role);
            userCredentialRepository.saveAndFlush(userCredential);
            Customer customer = mapToCustomer(userCredential, registerRequest);
            customerService.createCustomer(customer);
            accountService.createAccount(customer.getId());
            return mapToRegisterCustomer(userCredential, registerRequest);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User Already Exist");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = getAuthentication(loginRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            AppUser appUser = (AppUser) authentication.getPrincipal();
            String token = jwtUtil.generateToken(appUser);
            return mapToLoginResponse(appUser.getRole(), token);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid email or password", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }

    }

    @Override
    public RegisterResponse registerAdmin(AuthAdminRequest authAdminRequest) {
        try {
            Role role = getRole(ERole.ROLE_ADMIN);

            String passwordHashed = getEncryptPassword(authAdminRequest);
            UserCredential userCredential = mapToUserCredential(authAdminRequest, passwordHashed, role);
            userCredentialRepository.saveAndFlush(userCredential);

            Admin admin = mapToAdminRequest(authAdminRequest, userCredential);
            adminService.createSuperAdmin(admin);

            return mapToRegisterResponse(userCredential, authAdminRequest);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User admin already exist");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", e);
        }
    }
}
