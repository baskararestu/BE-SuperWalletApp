package com.enigma.superwallet.service;

import com.enigma.superwallet.dto.request.ForgotPasswordRequest;
import com.enigma.superwallet.dto.response.ForgetPasswordResponse;

public interface ForgotPasswordService {
    ForgetPasswordResponse resetPassword(ForgotPasswordRequest forgotPasswordRequest);
    void sendNewPassword(String email, String newPassword);
    String randomPassword(int length);
}
