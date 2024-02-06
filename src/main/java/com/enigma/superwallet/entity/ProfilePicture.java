package com.enigma.superwallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "m_profile_picture")
public class ProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Override
    public String toString() {
        return "ProfilePicture{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
