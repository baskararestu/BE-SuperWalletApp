package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.dto.request.LoginRequest;
import com.enigma.superwallet.dto.request.RegisterRequest;
import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.dto.response.LoginResponse;
import com.enigma.superwallet.dto.response.RegisterResponse;
import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.entity.Role;
import com.enigma.superwallet.entity.UserCredential;
import com.enigma.superwallet.repository.UserCredentialRepository;
import com.enigma.superwallet.security.JwtUtil;
import com.enigma.superwallet.service.AuthService;
import com.enigma.superwallet.service.CustomerService;
import com.enigma.superwallet.service.RoleService;
import com.enigma.superwallet.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ValidationUtil validationUtil;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        try {
            validationUtil.validate(registerRequest);
            Role role = Role.builder()
                    .role(ERole.ROLE_CUSTOMER)
                    .build();
            Role roleSaved = roleService.getOrSave(role);

            UserCredential userCredential = UserCredential.builder()
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(roleSaved)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userCredentialRepository.saveAndFlush(userCredential);

            Customer customer = Customer.builder()
                    .userCredential(userCredential)
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .birthDate(LocalDate.parse(registerRequest.getBirthDate()))
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .isActive(true)
                    .gender(registerRequest.getGender())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            customerService.createCustomer(customer);

            return RegisterResponse.builder()
                    .email(userCredential.getEmail())
                    .role(userCredential.getRole().getRole().toString())
                    .build();
        }catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"User Already Exist");
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        validationUtil.validate(loginRequest);
        System.out.println("2");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail().toLowerCase(),loginRequest.getPassword()));
        System.out.println("1");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUser appUser = (AppUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(appUser);



//        validationUtil.validate(request);
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                request.getEmail().toLowerCase(),
//                request.getPassword()
//        ));
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        AppUser appUser = (AppUser) authentication.getPrincipal();
//        String token = jwtUtil.generateToken(appUser);


        List<CustomerResponse> customerResponse = customerService.getAll();
        String email = loginRequest.getEmail();
        String firstName = customerResponse.stream().filter(customerResponse1 -> customerResponse1.getUserCredential().getEmail().equals(email)).toList().get(0).getFirstName();
        String lastName = customerResponse.stream().filter(customerResponse1 -> customerResponse1.getUserCredential().getEmail().equals(email)).toList().get(0).getLastName();
        return LoginResponse.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .token(token)
                .role(appUser.getRole().name())
                .build();
    }
}
