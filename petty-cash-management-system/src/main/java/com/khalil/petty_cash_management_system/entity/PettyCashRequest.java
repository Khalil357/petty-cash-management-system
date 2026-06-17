package com.khalil.petty_cash_management_system.entity;

import com.khalil.petty_cash_management_system.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "petty_cash_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestNumber;

    private BigDecimal amount;

    private String purpose;

    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @PrePersist
    public void prePersist() {
        if (requestDate == null) requestDate = LocalDate.now();
        if (status == null) status = RequestStatus.PENDING;
    }
}