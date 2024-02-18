package com.enigma.superwallet.controllers;

import com.enigma.superwallet.service.impl.FirebaseServices;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FirebaseController {
    private final FirebaseServices firebaseServices;

    @PostMapping("/profile/picture")
    public Object uploadFirebase(@RequestParam("file")MultipartFile multipartFile) {
        return firebaseServices.upload(multipartFile);
    }
}
