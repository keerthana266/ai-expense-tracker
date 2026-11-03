package com.expensetracker.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    // User who owns this account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String upiId;

    @Column(nullable = false)
    private Double currentBalance;

    @Column(nullable = false)
    private String accountType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean primaryAccount = false;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
}