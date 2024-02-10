package com.enigma.superwallet.entity;

import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "m_dummy_bank")
public class DummyBank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String bankNumber;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private Double balance;
}
