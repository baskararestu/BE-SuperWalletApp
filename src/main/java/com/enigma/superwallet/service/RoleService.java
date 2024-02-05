package com.enigma.superwallet.service;

import com.enigma.superwallet.entity.Role;

public interface RoleService {
    Role getOrSave(Role role);
}
