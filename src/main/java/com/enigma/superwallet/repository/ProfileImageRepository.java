package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfilePicture,String> {
    Optional<ProfilePicture> findByName(String name);
}
