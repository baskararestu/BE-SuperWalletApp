package com.enigma.superwallet.controllers;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.ForgotPasswordRequest;
import com.enigma.superwallet.dto.response.ForgetPasswordResponse;
import com.enigma.superwallet.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.PASSWORD)
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping
    public ForgetPasswordResponse resetPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return forgotPasswordService.resetPassword(forgotPasswordRequest);
    }
}
