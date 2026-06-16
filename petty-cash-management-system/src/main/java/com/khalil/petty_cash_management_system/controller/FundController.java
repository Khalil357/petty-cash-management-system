package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.FundReplenishmentDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashFund;
import com.khalil.petty_cash_management_system.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    @PostMapping("/replenish")
    public PettyCashFund replenish(
            @RequestBody FundReplenishmentDTO dto
    ) {
        return fundService.replenishFund(dto);
    }
}