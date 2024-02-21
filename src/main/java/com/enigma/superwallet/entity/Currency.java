package com.enigma.superwallet.entity;

import com.enigma.superwallet.constant.ECurrencyCode;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "m_currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ECurrencyCode code;

    @Column(nullable = false)
    private String name;
    
}
