package com.enigma.superwallet.entity;

import com.enigma.superwallet.constant.ETransactionType;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "m_transaction_type")
public class TransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private ETransactionType transactionType;
}
