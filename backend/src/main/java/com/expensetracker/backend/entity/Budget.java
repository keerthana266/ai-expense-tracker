package com.expensetracker.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================================
    // USER
    // ================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================================
    // CATEGORY
    // ================================

    @Column(nullable = false)
    private String category;

    // ================================
    // LIMIT
    // ================================

    @Column(nullable = false)
    private Double limitAmount;

    // ================================
    // PERIOD
    // ================================

    @Column(nullable = false)
    private String period;

    // ================================
    // CREATED DATE
    // ================================

    private LocalDateTime createdAt;
}