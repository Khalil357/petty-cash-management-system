package com.khalil.petty_cash_management_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundReplenishmentDTO {

    private BigDecimal amount;

    private String description;
}