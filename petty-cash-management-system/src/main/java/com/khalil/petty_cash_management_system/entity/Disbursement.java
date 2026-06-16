package com.khalil.petty_cash_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String referenceNumber;

    private LocalDateTime disbursementDate;

    @OneToOne
    @JoinColumn(name = "request_id")
    private PettyCashRequest request;

    @ManyToOne
    @JoinColumn(name = "accountant_id")
    private User accountant;

    @PrePersist
    public void prePersist() {
        disbursementDate = LocalDateTime.now();
    }
}