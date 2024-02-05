package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.entity.Role;
import com.enigma.superwallet.repository.RoleRepository;
import com.enigma.superwallet.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getOrSave(Role role) {
        Optional<Role> optionalRole = roleRepository.findByRole(role.getRole());
        if(!optionalRole.isEmpty()){
            return optionalRole.get();
        }
        return roleRepository.save(role);
    }
}
