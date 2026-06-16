package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.DisbursementRequestDTO;
import com.khalil.petty_cash_management_system.entity.Disbursement;
import com.khalil.petty_cash_management_system.service.DisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disbursements")
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;

    @PostMapping
    public Disbursement disburse(
            @RequestBody DisbursementRequestDTO dto
    ) {
        return disbursementService.disburseCash(dto);
    }
}