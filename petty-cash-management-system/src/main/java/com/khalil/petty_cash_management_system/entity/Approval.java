package com.khalil.petty_cash_management_system.entity;

import com.khalil.petty_cash_management_system.enums.Decision;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private String comments;

    private LocalDateTime approvalDate;

    @OneToOne
    @JoinColumn(name = "request_id")
    private PettyCashRequest request;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @PrePersist
    public void prePersist() {
        if (approvalDate == null) approvalDate = LocalDateTime.now();
    }
}