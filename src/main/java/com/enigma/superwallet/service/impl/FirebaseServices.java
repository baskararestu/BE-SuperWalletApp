package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.response.ProfilePictureResponse;
import com.enigma.superwallet.entity.ProfilePicture;
import com.enigma.superwallet.repository.ProfileImageRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseServices {

    private final ProfileImageRepository profileImageRepository;

    public ProfilePictureResponse upload(MultipartFile multipartFile) {

        String fileName = multipartFile.getOriginalFilename();
        try {
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));

            File file = this.convertToFile(multipartFile, fileName);
            String TEMP_URL = this.uploadFile(file, fileName);
            file.delete();

            ProfilePicture profilePicture = ProfilePicture.builder().name(TEMP_URL).uploadedAt(LocalDateTime.now()).build();
            profileImageRepository.saveAndFlush(profilePicture);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProfilePictureResponse.builder()
                .fileName(fileName)
                .dateTime(LocalDateTime.now())
                .build();
    }


    private String uploadFile(File file, String fileName) throws IOException {
        BlobId blobId = BlobId.of("superwallet-83957.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("/home/user/Batch14/finalProject/SuperWallet/v5/super-wallet-backend/src/main/resources/superwallet-83957-firebase-adminsdk-v86yl-1c4cfaf2ed.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return String.format("https://firebasestorage.googleapis.com/v0/b/superwallet-83957.appspot.com/o/%s?alt=media", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
