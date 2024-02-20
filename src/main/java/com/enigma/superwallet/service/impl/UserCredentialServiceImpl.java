    package com.enigma.superwallet.service.impl;

    import com.enigma.superwallet.constant.ERole;
    import com.enigma.superwallet.entity.AppUser;
    import com.enigma.superwallet.entity.UserCredential;
    import com.enigma.superwallet.repository.UserCredentialRepository;
    import com.enigma.superwallet.security.JwtUtil;
    import com.enigma.superwallet.service.UserCredentialService;
    import com.enigma.superwallet.util.ValidationUtil;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.web.server.ResponseStatusException;

    @Service
    @RequiredArgsConstructor
    public class UserCredentialServiceImpl implements UserCredentialService {
        private final UserCredentialRepository userCredentialRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final ValidationUtil util;


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

        @Override
        public UserCredential createPin(String pin) {

            String token = util.extractTokenFromHeader();

            String userId = jwtUtil.getUserInfoByToken(token).get("userId");

            UserCredential userCredential = userCredentialRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            userCredential.setPin(pin);

            return userCredentialRepository.save(userCredential);
        }
    }