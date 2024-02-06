package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.entity.Admin;

public interface AdminService {
    AdminResponse createSuperAdmin(Admin admin);
}
