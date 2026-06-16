package com.khalil.petty_cash_management_system.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private Long totalUsers;
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long rejectedRequests;
    private Long disbursedRequests;
    private BigDecimal currentFundBalance;
}