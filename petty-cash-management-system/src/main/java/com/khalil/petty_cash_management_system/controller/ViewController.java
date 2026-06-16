package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.repository.ApprovalRepository;
import com.khalil.petty_cash_management_system.repository.DisbursementRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashFundRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashRequestRepository;
import com.khalil.petty_cash_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final PettyCashRequestRepository requestRepository;
    private final ApprovalRepository approvalRepository;
    private final DisbursementRepository disbursementRepository;
    private final PettyCashFundRepository fundRepository;

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAllAttributes(dashboardService.getDashboardStats());
        return "dashboard-admin";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        model.addAllAttributes(dashboardService.getManagerDashboardStats());
        return "dashboard-manager";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model) {
        // Hardcoding employeeId = 1L for now since there's no auth context
        model.addAllAttributes(dashboardService.getEmployeeDashboardStats(1L));
        return "dashboard-employee";
    }

    @GetMapping("/requests")
    public String requests(Model model) {

        model.addAttribute(
                "requests",
                requestRepository.findAll()
        );

        return "requests";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {

        model.addAttribute(
                "approvals",
                approvalRepository.findAll()
        );

        return "approvals";
    }

    @GetMapping("/disbursements")
    public String disbursements(Model model) {

        model.addAttribute(
                "disbursements",
                disbursementRepository.findAll()
        );

        return "disbursements";
    }

    @GetMapping("/funds")
    public String funds(Model model) {

        model.addAttribute(
                "fund",
                fundRepository.findById(1L).orElse(null)
        );

        return "funds";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }
}