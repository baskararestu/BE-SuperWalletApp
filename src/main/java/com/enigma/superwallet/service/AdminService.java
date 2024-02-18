package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.AdminRequest;
import com.enigma.superwallet.dto.response.AdminResponse;
import com.enigma.superwallet.entity.Admin;

import java.util.List;

public interface AdminService {
    AdminResponse createSuperAdmin(Admin admin);
    List<AdminResponse> getAllAdmin();
    AdminResponse updateAdmin(AdminRequest adminRequest);
    Boolean softDeleteAdmin(String id);
}
