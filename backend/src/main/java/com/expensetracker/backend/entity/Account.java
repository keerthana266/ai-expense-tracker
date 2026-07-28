package com.expensetracker.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private Double currentBalance;

    @Column(nullable = false)
    private String accountType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}