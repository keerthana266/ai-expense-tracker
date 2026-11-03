package com.expensetracker.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private String aiCategory;

    @Column(nullable = false)
    private Double aiConfidence;

    @Column(length = 2000)
    private String notificationText;

    @Column(length = 1000)
    private String aiReasoning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "account_id")
   @JsonIgnoreProperties({
        "transactions",
        "user",
        "hibernateLazyInitializer",
        "handler"
   })
   private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({
            "transactions",
            "hibernateLazyInitializer",
            "handler"
    })
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    @JsonIgnoreProperties({
            "transactions",
            "hibernateLazyInitializer",
            "handler"
    })
    private Merchant merchant;
}