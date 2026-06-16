package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.ApprovalRequestDTO;
import com.khalil.petty_cash_management_system.entity.Approval;

import java.util.List;

public interface ApprovalService {

    Approval approveRequest(ApprovalRequestDTO dto);

    List<Approval> getAllApprovals();
}