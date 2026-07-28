package com.expensetracker.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cash_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double cashBalance;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}