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

    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private String expDate;
}
