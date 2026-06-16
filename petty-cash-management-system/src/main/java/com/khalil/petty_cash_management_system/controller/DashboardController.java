package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.DashboardDTO;
import com.khalil.petty_cash_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDTO getDashboard() {
        return dashboardService.getDashboard();
    }
}