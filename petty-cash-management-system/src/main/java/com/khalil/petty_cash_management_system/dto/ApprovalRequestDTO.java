package com.khalil.petty_cash_management_system.dto;

import com.khalil.petty_cash_management_system.enums.Decision;
import lombok.Data;

@Data
public class ApprovalRequestDTO {

    private Long requestId;
    private Long managerId;
    private Decision decision;
    private String comments;
}