package com.enigma.superwallet.repository;

import com.enigma.superwallet.constant.ERole;
import com.enigma.superwallet.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {
    Optional<UserCredential> findByEmail(String email);
    //Optional<UserCredential> findByEmail(String email);
    boolean existsByRole_RoleName(ERole roleName);
}
