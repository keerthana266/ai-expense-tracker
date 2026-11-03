package com.expensetracker.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String merchantType;

    private String logoUrl;

    @Builder.Default
    private Boolean verified = false;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
}