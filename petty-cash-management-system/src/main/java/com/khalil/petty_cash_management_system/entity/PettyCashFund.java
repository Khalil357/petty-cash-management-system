package com.khalil.petty_cash_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "petty_cash_funds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    public void prePersist() {
        lastUpdated = LocalDateTime.now();

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}