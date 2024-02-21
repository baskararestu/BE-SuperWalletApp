package com.enigma.superwallet.service.impl;

import com.enigma.superwallet.dto.request.ProfilePictureRequest;
import com.enigma.superwallet.dto.response.ProfilePictureResponse;
import com.enigma.superwallet.entity.ProfilePicture;
import com.enigma.superwallet.repository.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfilePictureService {
    private final Path fileStorageLocation = Paths.get("/home/user/Batch14/finalProject/SuperWallet/v2/super-wallet-backend/src/main/resources/static");
    private final ProfileImageRepository profileImageRepository;

    public ProfilePictureResponse uploadPicture(ProfilePictureRequest profilePictureRequest) {
        String mimiType = profilePictureRequest.getImage().getContentType();

        if(mimiType == null || (!mimiType.startsWith("image/"))) {
            throw new RuntimeException("Invalid Upload, Only Image");
        }
        try {
            Path targetLocation = this.fileStorageLocation.resolve(profilePictureRequest.getImage().getOriginalFilename());
            Files.copy(profilePictureRequest.getImage().getInputStream(),targetLocation, StandardCopyOption.REPLACE_EXISTING);
            ProfilePicture profilePicture = ProfilePicture.builder()
                    .name(profilePictureRequest.getImage().getOriginalFilename())
                    .uploadedAt(LocalDateTime.now()).build();
            profileImageRepository.saveAndFlush(profilePicture);

        }catch (IOException e) {
            throw new RuntimeException("Could Not Store" + profilePictureRequest.getImage().getOriginalFilename());
        }

        return ProfilePictureResponse.builder()
                .fileName(profilePictureRequest.getImage().getOriginalFilename())
                .dateTime(LocalDateTime.now()).build();
    }

    public Resource getProfilePicture(String name) throws FileNotFoundException, MalformedURLException {
        try{
            Path targetLocation = this.fileStorageLocation.resolve(name).normalize();
            Resource resource = new UrlResource(targetLocation.toUri());

            if(resource.exists()){
                return resource;
            }else {
                throw new FileNotFoundException("File not Found" + name);
            }
        }catch (MalformedURLException e) {
            throw new FileNotFoundException("File not Found" + name);
        }
    }
}
