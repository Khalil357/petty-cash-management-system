package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.entity.*;
import com.khalil.petty_cash_management_system.enums.Decision;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.enums.TransactionType;
import com.khalil.petty_cash_management_system.repository.*;
import com.khalil.petty_cash_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final DashboardService dashboardService;
    private final PettyCashRequestRepository requestRepository;
    private final ApprovalRepository approvalRepository;
    private final DisbursementRepository disbursementRepository;
    private final PettyCashFundRepository fundRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FundTransactionRepository transactionRepository;

    // ─── Auth helpers ───────────────────────────────────────────

    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }

    // ─── Login ──────────────────────────────────────────────────

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password");
        if (logout != null) model.addAttribute("logout", "You have been logged out");
        return "login";
    }

    // ─── Dashboard redirect by role ─────────────────────────────

    @GetMapping("/dashboard")
    public String redirectByRole(Authentication auth) {
        if (auth == null) return "redirect:/login";
        if (hasRole(auth, "ADMIN")) return "redirect:/admin/dashboard";
        if (hasRole(auth, "MANAGER")) return "redirect:/manager/dashboard";
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

    // ─── Admin Dashboard ────────────────────────────────────────

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAllAttributes(dashboardService.getDashboardStats());
        model.addAttribute("recentDisbursements",
            disbursementRepository.findAll().stream()
                .sorted((a, b) -> b.getDisbursementDate().compareTo(a.getDisbursementDate()))
                .limit(5)
                .toList());
        return "dashboard-admin";
    }

    // ─── Manager Dashboard ──────────────────────────────────────

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        model.addAllAttributes(dashboardService.getManagerDashboardStats());
        return "dashboard-manager";
    }

    // ─── Employee Dashboard ─────────────────────────────────────

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";
        model.addAllAttributes(dashboardService.getEmployeeDashboardStats(user.getId()));
        return "dashboard-employee";
    }

    // ─── Requests ───────────────────────────────────────────────

    @GetMapping("/requests")
    public String requests(Model model, Authentication auth) {
        if (hasRole(auth, "EMPLOYEE") && !hasRole(auth, "ADMIN") && !hasRole(auth, "MANAGER")) {
            User user = getCurrentUser(auth);
            if (user != null) {
                model.addAttribute("requests",
                    requestRepository.findByEmployeeIdOrderByRequestDateDesc(user.getId()));
                model.addAttribute("isMyRequests", true);
                model.addAttribute("currentUser", user);
                return "requests";
            }
        }
        model.addAttribute("requests", requestRepository.findAll());
        model.addAttribute("isMyRequests", false);
        return "requests";
    }

    @PostMapping("/employee/requests/create")
    public String createRequest(Authentication auth,
                                @RequestParam BigDecimal amount,
                                @RequestParam String purpose,
                                RedirectAttributes ra) {
        User user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        PettyCashRequest request = PettyCashRequest.builder()
            .requestNumber("REQ-" + System.currentTimeMillis() % 100000)
            .amount(amount)
            .purpose(purpose)
            .employee(user)
            .requestDate(LocalDate.now())
            .status(RequestStatus.PENDING)
            .build();
        requestRepository.save(request);
        ra.addFlashAttribute("success", "Request submitted successfully!");
        return "redirect:/requests";
    }

    // ─── Approvals ──────────────────────────────────────────────

    @GetMapping("/approvals")
    public String approvals(Model model, Authentication auth) {
        if (hasRole(auth, "MANAGER") && !hasRole(auth, "ADMIN")) {
            model.addAttribute("pendingRequests",
                requestRepository.findByStatus(RequestStatus.PENDING));
        }
        model.addAttribute("approvals", approvalRepository.findAll());
        return "approvals";
    }

    @PostMapping("/manager/approvals/{id}/approve")
    public String approveRequest(Authentication auth, @PathVariable Long id,
                                 RedirectAttributes ra) {
        User manager = getCurrentUser(auth);
        if (manager == null) return "redirect:/login";

        PettyCashRequest request = requestRepository.findById(id).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            ra.addFlashAttribute("error", "Request not found or already processed");
            return "redirect:/approvals";
        }

        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);

        Approval approval = Approval.builder()
            .request(request)
            .manager(manager)
            .decision(Decision.APPROVE)
            .comments("Approved by " + manager.getFullName())
            .build();
        approvalRepository.save(approval);

        ra.addFlashAttribute("success", "Request #" + request.getRequestNumber() + " approved");
        return "redirect:/approvals";
    }

    @PostMapping("/manager/approvals/{id}/reject")
    public String rejectRequest(Authentication auth, @PathVariable Long id,
                                @RequestParam(defaultValue = "Rejected") String comments,
                                RedirectAttributes ra) {
        User manager = getCurrentUser(auth);
        if (manager == null) return "redirect:/login";

        PettyCashRequest request = requestRepository.findById(id).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            ra.addFlashAttribute("error", "Request not found or already processed");
            return "redirect:/approvals";
        }

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        Approval approval = Approval.builder()
            .request(request)
            .manager(manager)
            .decision(Decision.REJECT)
            .comments(comments)
            .build();
        approvalRepository.save(approval);

        ra.addFlashAttribute("success", "Request #" + request.getRequestNumber() + " rejected");
        return "redirect:/approvals";
    }

    // ─── Disbursements ──────────────────────────────────────────

    @GetMapping("/disbursements")
    public String disbursements(Model model) {
        model.addAttribute("disbursements", disbursementRepository.findAll());
        model.addAttribute("approvedRequests",
            requestRepository.findByStatus(RequestStatus.APPROVED));
        model.addAttribute("fund", fundRepository.findById(1L).orElse(null));
        return "disbursements";
    }

    @PostMapping("/admin/disbursements/create")
    public String createDisbursement(Authentication auth,
                                     @RequestParam Long requestId,
                                     RedirectAttributes ra) {
        User accountant = getCurrentUser(auth);
        if (accountant == null) return "redirect:/login";

        PettyCashRequest request = requestRepository.findById(requestId).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.APPROVED) {
            ra.addFlashAttribute("error", "Invalid request or request not approved");
            return "redirect:/disbursements";
        }

        PettyCashFund fund = fundRepository.findById(1L).orElse(null);
        if (fund == null || fund.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            ra.addFlashAttribute("error", "Insufficient fund balance ($"
                + (fund != null ? fund.getCurrentBalance() : "0.00") + ")");
            return "redirect:/disbursements";
        }

        // Deduct balance
        fund.setCurrentBalance(fund.getCurrentBalance().subtract(request.getAmount()));
        fundRepository.save(fund);

        // Record fund transaction
        FundTransaction ft = FundTransaction.builder()
            .fund(fund)
            .amount(request.getAmount())
            .transactionType(TransactionType.DISBURSEMENT)
            .description("Disbursement for: " + request.getPurpose())
            .build();
        transactionRepository.save(ft);

        // Update request status
        request.setStatus(RequestStatus.DISBURSED);
        requestRepository.save(request);

        // Create disbursement record
        Disbursement disbursement = Disbursement.builder()
            .request(request)
            .accountant(accountant)
            .amount(request.getAmount())
            .referenceNumber("DISB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .build();
        disbursementRepository.save(disbursement);

        ra.addFlashAttribute("success",
            "Disbursed $" + request.getAmount() + " to " + request.getEmployee().getFullName());
        return "redirect:/disbursements";
    }

    // ─── Funds ──────────────────────────────────────────────────

    @GetMapping("/funds")
    public String funds(Model model) {
        model.addAttribute("fund", fundRepository.findById(1L).orElse(null));
        model.addAttribute("transactions",
            transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList());
        return "funds";
    }

    @PostMapping("/admin/funds/replenish")
    public String replenishFund(@RequestParam BigDecimal amount,
                                @RequestParam String description,
                                RedirectAttributes ra) {
        PettyCashFund fund = fundRepository.findById(1L).orElse(null);
        if (fund == null) {
            fund = PettyCashFund.builder()
                .currentBalance(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .build();
        }
        fund.setCurrentBalance(fund.getCurrentBalance().add(amount));
        fundRepository.save(fund);

        FundTransaction ft = FundTransaction.builder()
            .fund(fund)
            .amount(amount)
            .transactionType(TransactionType.REPLENISHMENT)
            .description(description)
            .build();
        transactionRepository.save(ft);

        ra.addFlashAttribute("success", "Fund replenished by $" + amount);
        return "redirect:/funds";
    }

    // ─── Reports ────────────────────────────────────────────────

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAllAttributes(dashboardService.getDashboardStats());
        model.addAttribute("transactions",
            transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList());
        return "reports";
    }

    // ─── Transactions ───────────────────────────────────────────

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions",
            transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .toList());
        return "transactions";
    }

    // ─── Users (Admin only — secured by hasRole in SecurityConfig) ─

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        return "users";
    }

    @PostMapping("/admin/users/create")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String phone,
                             @RequestParam Long roleId,
                             RedirectAttributes ra) {
        if (userRepository.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Email already exists");
            return "redirect:/users";
        }
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            ra.addFlashAttribute("error", "Invalid role");
            return "redirect:/users";
        }
        User user = User.builder()
            .fullName(fullName)
            .email(email)
            .password(passwordEncoder.encode(password))
            .phone(phone)
            .role(role)
            .active(true)
            .build();
        userRepository.save(user);
        ra.addFlashAttribute("success", "User " + fullName + " created successfully!");
        return "redirect:/users";
    }
}
