package com.enigma.superwallet.controller;

import com.enigma.superwallet.constant.AppPath;
import com.enigma.superwallet.dto.request.ProfilePictureRequest;
import com.enigma.superwallet.dto.response.CommonResponse;
import com.enigma.superwallet.dto.response.ProfilePictureResponse;
import com.enigma.superwallet.service.impl.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppPath.PROFILE_PICTURE)
public class ProfilePictureController {
    private final ProfilePictureService profilePictureService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) {
        ProfilePictureRequest build = ProfilePictureRequest.builder()
                .image(file).build();
        ProfilePictureResponse profilePictureResponse = profilePictureService.uploadPicture(build);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(CommonResponse.<ProfilePictureResponse>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Success Uploaded File")
                        .data(profilePictureResponse)
                        .build());
    }

    @GetMapping(value = "get-profile/{file}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProfilePicture(@PathVariable String file) {
        Resource resource;
        try {
            resource = profilePictureService.getProfilePicture(file);
        }catch (FileNotFoundException | MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


}
