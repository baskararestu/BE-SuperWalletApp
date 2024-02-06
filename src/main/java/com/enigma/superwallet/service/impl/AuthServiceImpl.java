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
    private final CustomerService customerService;
    private final AuthenticationManager authenticationManager;
    private final ValidationUtil validationUtil;
    private final JwtUtil jwtUtil;

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
