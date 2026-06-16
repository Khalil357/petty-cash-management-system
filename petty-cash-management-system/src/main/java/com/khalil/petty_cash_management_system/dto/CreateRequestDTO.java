package com.khalil.petty_cash_management_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRequestDTO {

    private Long employeeId;
    private BigDecimal amount;
    private String purpose;
}