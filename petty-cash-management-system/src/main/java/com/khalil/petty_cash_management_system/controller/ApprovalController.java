package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.ApprovalRequestDTO;
import com.khalil.petty_cash_management_system.entity.Approval;
import com.khalil.petty_cash_management_system.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public Approval approveRequest(
            @RequestBody ApprovalRequestDTO dto
    ) {
        return approvalService.approveRequest(dto);
    }

    @GetMapping
    public List<Approval> getAllApprovals() {
        return approvalService.getAllApprovals();
    }
}