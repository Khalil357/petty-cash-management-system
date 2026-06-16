package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.entity.PettyCashFund;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.repository.PettyCashFundRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashRequestRepository;
import com.khalil.petty_cash_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserRepository userRepository;
    private final PettyCashRequestRepository requestRepository;
    private final PettyCashFundRepository fundRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        Map<String, Object> report = new HashMap<>();

        report.put("totalUsers",
                userRepository.count());

        report.put("totalRequests",
                requestRepository.count());

        report.put("approvedRequests",
                requestRepository.countByStatus(RequestStatus.APPROVED));

        report.put("pendingRequests",
                requestRepository.countByStatus(RequestStatus.PENDING));

        report.put("rejectedRequests",
                requestRepository.countByStatus(RequestStatus.REJECTED));

        report.put("disbursedRequests",
                requestRepository.countByStatus(RequestStatus.DISBURSED));

        PettyCashFund fund =
                fundRepository.findById(1L).orElse(null);

        BigDecimal balance =
                fund != null ? fund.getCurrentBalance() : BigDecimal.ZERO;

        report.put("currentFundBalance", balance);

        return report;
    }
}