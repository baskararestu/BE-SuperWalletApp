package com.enigma.superwallet.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProfilePictureRequest {

    private MultipartFile image;

    @Override
    public String toString() {
        return "ProfilePictureRequest{" +
                "image=" + image +
                '}';
    }
}
