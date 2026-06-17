# Thymeleaf Completion & Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete the Petty Cash Management System frontend using Thymeleaf + Tailwind CSS, with Spring Security auth and realistic sample data.

**Architecture:** Server-side rendered Spring MVC with Thymeleaf templates. Spring Security form login for auth. Tailwind CSS (CDN) for styling. BCrypt passwords. Role-based URL access control.

**Tech Stack:** Java 21, Spring Boot 4.0, Thymeleaf, Spring Security, Tailwind CSS (CDN), MySQL

---

## File Structure

### Files to Create
- `src/main/resources/templates/login.html` — Login page
- `src/main/resources/templates/transactions.html` — Transaction history
- `src/main/resources/templates/users.html` — User management

### Files to Modify
- `src/main/java/.../config/SecurityConfig.java` — Form login, role access, password encoder
- `src/main/java/.../config/DataInitializer.java` — Seed all sample data
- `src/main/java/.../controller/ViewController.java` — Auth context, new routes, form handlers
- `src/main/resources/templates/dashboard-admin.html` — Dynamic table
- `src/main/resources/templates/dashboard-manager.html` — Fix employee name
- `src/main/resources/templates/dashboard-employee.html` — Link to new request
- `src/main/resources/templates/requests.html` — Full table + new request form
- `src/main/resources/templates/approvals.html` — Fix columns, add actions
- `src/main/resources/templates/disbursements.html` — Full table + disbursement form
- `src/main/resources/templates/funds.html` — Replenishment form
- `src/main/resources/templates/reports.html` — Dynamic data
- `src/main/resources/templates/fragments/sidebar.html` — Unified dark sidebar

---

### Task 1: Security Config — Form Login & Role Access

**Files:**
- Modify: `src/main/java/com/khalil/petty_cash_management_system/config/SecurityConfig.java`

- [ ] **Replace SecurityConfig with form login, role-based authorization, and password encoder**

```java
package com.khalil.petty_cash_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
```

### Task 2: ViewController — Auth-Aware Routing & Form Handlers

**Files:**
- Modify: `src/main/java/com/khalil/petty_cash_management_system/controller/ViewController.java`

- [ ] **Replace ViewController with auth-aware version including form POST handlers**

```java
package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.entity.*;
import com.khalil.petty_cash_management_system.enums.Decision;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.enums.TransactionType;
import com.khalil.petty_cash_management_system.repository.*;
import com.khalil.petty_cash_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    private final FundTransactionRepository transactionRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password");
        if (logout != null) model.addAttribute("logout", "You have been logged out");
        return "login";
    }

    @GetMapping("/dashboard")
    public String redirectByRole(Authentication auth) {
        if (auth == null) return "redirect:/login";
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = auth.getAuthorities().stream()
            .anyMatch(g -> g.getAuthority().equals("ROLE_MANAGER"));
        if (isAdmin) return "redirect:/admin/dashboard";
        if (isManager) return "redirect:/manager/dashboard";
        return "redirect:/employee/dashboard";
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAllAttributes(dashboardService.getDashboardStats());
        model.addAttribute("recentDisbursements",
            disbursementRepository.findAll().stream()
                .sorted((a, b) -> b.getDisbursementDate().compareTo(a.getDisbursementDate()))
                .limit(5).toList());
        return "dashboard-admin";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        model.addAllAttributes(dashboardService.getManagerDashboardStats());
        return "dashboard-manager";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Authentication auth) {
        // Will handle via model
        return "redirect:/employee/requests";
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        model.addAttribute("requests", requestRepository.findAll());
        return "requests";
    }

    @GetMapping("/employee/requests")
    public String employeeRequests(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        if (user != null) {
            model.addAttribute("requests",
                requestRepository.findByEmployeeIdOrderByRequestDateDesc(user.getId()));
        }
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
        return "redirect:/employee/requests";
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {
        model.addAttribute("approvals", approvalRepository.findAll());
        return "approvals";
    }

    @PostMapping("/manager/approvals/{id}/approve")
    public String approveRequest(Authentication auth, @PathVariable Long id, RedirectAttributes ra) {
        User manager = getCurrentUser(auth);
        if (manager == null) return "redirect:/login";
        PettyCashRequest request = requestRepository.findById(id).orElse(null);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            ra.addFlashAttribute("error", "Request not found or already processed");
            return "redirect:/manager/dashboard";
        }
        request.setStatus(RequestStatus.APPROVED);
        requestRepository.save(request);
        Approval approval = Approval.builder()
            .request(request).manager(manager)
            .decision(Decision.APPROVE).comments("Approved")
            .build();
        approvalRepository.save(approval);
        ra.addFlashAttribute("success", "Request #" + request.getRequestNumber() + " approved");
        return "redirect:/manager/dashboard";
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
            return "redirect:/manager/dashboard";
        }
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);
        Approval approval = Approval.builder()
            .request(request).manager(manager)
            .decision(Decision.REJECT).comments(comments)
            .build();
        approvalRepository.save(approval);
        ra.addFlashAttribute("success", "Request #" + request.getRequestNumber() + " rejected");
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/disbursements")
    public String disbursements(Model model) {
        model.addAttribute("disbursements", disbursementRepository.findAll());
        model.addAttribute("approvedRequests",
            requestRepository.findByStatus(RequestStatus.APPROVED));
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
            ra.addFlashAttribute("error", "Invalid request");
            return "redirect:/disbursements";
        }
        PettyCashFund fund = fundRepository.findById(1L).orElse(null);
        if (fund == null || fund.getCurrentBalance().compareTo(request.getAmount()) < 0) {
            ra.addFlashAttribute("error", "Insufficient fund balance");
            return "redirect:/disbursements";
        }
        fund.setCurrentBalance(fund.getCurrentBalance().subtract(request.getAmount()));
        fundRepository.save(fund);
        FundTransaction ft = FundTransaction.builder()
            .fund(fund).amount(request.getAmount())
            .transactionType(TransactionType.DISBURSEMENT)
            .description("Disbursement for: " + request.getPurpose())
            .build();
        transactionRepository.save(ft);
        request.setStatus(RequestStatus.DISBURSED);
        requestRepository.save(request);
        Disbursement disbursement = Disbursement.builder()
            .request(request).accountant(accountant)
            .amount(request.getAmount())
            .referenceNumber("DISB-" + UUID.randomUUID().toString().substring(0, 8))
            .build();
        disbursementRepository.save(disbursement);
        ra.addFlashAttribute("success", "Disbursed $" + request.getAmount() + " successfully");
        return "redirect:/disbursements";
    }

    @GetMapping("/funds")
    public String funds(Model model) {
        model.addAttribute("fund", fundRepository.findById(1L).orElse(null));
        model.addAttribute("transactions", transactionRepository.findAll());
        return "funds";
    }

    @PostMapping("/admin/funds/replenish")
    public String replenishFund(@RequestParam BigDecimal amount,
                                @RequestParam String description,
                                RedirectAttributes ra) {
        PettyCashFund fund = fundRepository.findById(1L).orElse(null);
        if (fund == null) {
            fund = PettyCashFund.builder().currentBalance(BigDecimal.ZERO).build();
        }
        fund.setCurrentBalance(fund.getCurrentBalance().add(amount));
        fundRepository.save(fund);
        FundTransaction ft = FundTransaction.builder()
            .fund(fund).amount(amount)
            .transactionType(TransactionType.REPLENISHMENT)
            .description(description)
            .build();
        transactionRepository.save(ft);
        ra.addFlashAttribute("success", "Fund replenished by $" + amount);
        return "redirect:/funds";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAllAttributes(dashboardService.getDashboardStats());
        model.addAttribute("transactions", transactionRepository.findAll());
        return "reports";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions", transactionRepository.findAll());
        return "transactions";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }
}
```

### Task 3: DataInitializer — Full Sample Data

**Files:**
- Modify: `src/main/java/com/khalil/petty_cash_management_system/config/DataInitializer.java`

- [ ] **Replace DataInitializer with comprehensive sample data**

```java
package com.khalil.petty_cash_management_system.config;

import com.khalil.petty_cash_management_system.entity.*;
import com.khalil.petty_cash_management_system.enums.*;
import com.khalil.petty_cash_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PettyCashRequestRepository requestRepository;
    private final ApprovalRepository approvalRepository;
    private final DisbursementRepository disbursementRepository;
    private final PettyCashFundRepository fundRepository;
    private final FundTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) return;

        // 1. Roles
        Role adminRole = roleRepository.save(Role.builder().name(RoleName.ADMIN).description("Administrator").build());
        Role managerRole = roleRepository.save(Role.builder().name(RoleName.MANAGER).description("Manager").build());
        Role employeeRole = roleRepository.save(Role.builder().name(RoleName.EMPLOYEE).description("Employee").build());

        String pw = passwordEncoder.encode("password123");

        // 2. Users
        User admin = userRepository.save(User.builder().fullName("Alice Admin").email("admin@pettycash.com").password(pw).phone("+1-555-0100").role(adminRole).active(true).build());
        User admin2 = userRepository.save(User.builder().fullName("Bob Admin").email("bob@pettycash.com").password(pw).phone("+1-555-0101").role(adminRole).active(true).build());
        User manager = userRepository.save(User.builder().fullName("Carol Manager").email("manager@pettycash.com").password(pw).phone("+1-555-0102").role(managerRole).active(true).build());
        User manager2 = userRepository.save(User.builder().fullName("David Manager").email("david@pettycash.com").password(pw).phone("+1-555-0103").role(managerRole).active(true).build());
        User emp1 = userRepository.save(User.builder().fullName("Eve Employee").email("eve@pettycash.com").password(pw).phone("+1-555-0104").role(employeeRole).active(true).build());
        User emp2 = userRepository.save(User.builder().fullName("Frank Employee").email("frank@pettycash.com").password(pw).phone("+1-555-0105").role(employeeRole).active(true).build());
        User emp3 = userRepository.save(User.builder().fullName("Grace Employee").email("grace@pettycash.com").password(pw).phone("+1-555-0106").role(employeeRole).active(true).build());

        // 3. Petty Cash Fund
        PettyCashFund fund = fundRepository.save(PettyCashFund.builder().currentBalance(new BigDecimal("10000.00")).lastUpdated(LocalDateTime.now()).build());

        // 4. Fund Transactions (Replenishments)
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(new BigDecimal("5000.00")).transactionType(TransactionType.REPLENISHMENT).description("Initial fund allocation").transactionDate(LocalDateTime.now().minusDays(30)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(new BigDecimal("5000.00")).transactionType(TransactionType.REPLENISHMENT).description("Additional fund top-up").transactionDate(LocalDateTime.now().minusDays(15)).build());

        // 5. Requests
        PettyCashRequest r1 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-001").amount(new BigDecimal("150.00")).purpose("Office supplies - printer ink and paper").employee(emp1).requestDate(LocalDate.now().minusDays(5)).status(RequestStatus.DISBURSED).build());
        PettyCashRequest r2 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-002").amount(new BigDecimal("85.50")).purpose("Client lunch meeting").employee(emp2).requestDate(LocalDate.now().minusDays(4)).status(RequestStatus.DISBURSED).build());
        PettyCashRequest r3 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-003").amount(new BigDecimal("200.00")).purpose("Fuel reimbursement - business trip").employee(emp1).requestDate(LocalDate.now().minusDays(3)).status(RequestStatus.DISBURSED).build());
        PettyCashRequest r4 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-004").amount(new BigDecimal("45.00")).purpose("Taxi fare to client site").employee(emp3).requestDate(LocalDate.now().minusDays(2)).status(RequestStatus.APPROVED).build());
        PettyCashRequest r5 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-005").amount(new BigDecimal("320.00")).purpose("Team building event supplies").employee(emp2).requestDate(LocalDate.now().minusDays(1)).status(RequestStatus.APPROVED).build());
        PettyCashRequest r6 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-006").amount(new BigDecimal("75.00")).purpose("Stationery for monthly report").employee(emp3).requestDate(LocalDate.now()).status(RequestStatus.PENDING).build());
        PettyCashRequest r7 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-007").amount(new BigDecimal("120.00")).purpose("Software license renewal").employee(emp1).requestDate(LocalDate.now()).status(RequestStatus.PENDING).build());
        PettyCashRequest r8 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-008").amount(new BigDecimal("50.00")).purpose("Team breakfast meeting").employee(emp2).requestDate(LocalDate.now().minusDays(6)).status(RequestStatus.REJECTED).build());
        PettyCashRequest r9 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-009").amount(new BigDecimal("180.00")).purpose("Conference registration fee").employee(emp3).requestDate(LocalDate.now().minusDays(7)).status(RequestStatus.REJECTED).build());
        PettyCashRequest r10 = requestRepository.save(PettyCashRequest.builder().requestNumber("REQ-010").amount(new BigDecimal("95.00")).purpose("Books and reference materials").employee(emp1).requestDate(LocalDate.now().minusDays(8)).status(RequestStatus.DISBURSED).build());

        // 6. Approvals
        approvalRepository.save(Approval.builder().request(r1).manager(manager).decision(Decision.APPROVE).comments("Approved for office use").approvalDate(LocalDateTime.now().minusDays(4)).build());
        approvalRepository.save(Approval.builder().request(r2).manager(manager).decision(Decision.APPROVE).comments("Client entertainment approved").approvalDate(LocalDateTime.now().minusDays(3)).build());
        approvalRepository.save(Approval.builder().request(r3).manager(manager).decision(Decision.APPROVE).comments("Business travel approved").approvalDate(LocalDateTime.now().minusDays(2)).build());
        approvalRepository.save(Approval.builder().request(r4).manager(manager2).decision(Decision.APPROVE).comments("Transport approved").approvalDate(LocalDateTime.now().minusDays(1)).build());
        approvalRepository.save(Approval.builder().request(r5).manager(manager2).decision(Decision.APPROVE).comments("Team event approved").approvalDate(LocalDateTime.now()).build());
        approvalRepository.save(Approval.builder().request(r8).manager(manager).decision(Decision.REJECT).comments("Budget exceeded for this category").approvalDate(LocalDateTime.now().minusDays(5)).build());
        approvalRepository.save(Approval.builder().request(r9).manager(manager2).decision(Decision.REJECT).comments("Conference already covered by department budget").approvalDate(LocalDateTime.now().minusDays(6)).build());
        approvalRepository.save(Approval.builder().request(r10).manager(manager).decision(Decision.APPROVE).comments("Educational materials approved").approvalDate(LocalDateTime.now().minusDays(7)).build());

        // 7. Disbursements
        disbursementRepository.save(Disbursement.builder().request(r1).accountant(admin).amount(r1.getAmount()).referenceNumber("DISB-a1b2c3d4").disbursementDate(LocalDateTime.now().minusDays(3)).build());
        disbursementRepository.save(Disbursement.builder().request(r2).accountant(admin).amount(r2.getAmount()).referenceNumber("DISB-e5f6g7h8").disbursementDate(LocalDateTime.now().minusDays(2)).build());
        disbursementRepository.save(Disbursement.builder().request(r3).accountant(admin).amount(r3.getAmount()).referenceNumber("DISB-i9j0k1l2").disbursementDate(LocalDateTime.now().minusDays(1)).build());
        disbursementRepository.save(Disbursement.builder().request(r10).accountant(admin2).amount(r10.getAmount()).referenceNumber("DISB-m3n4o5p6").disbursementDate(LocalDateTime.now().minusDays(6)).build());

        // 8. Fund Transactions (Disbursements)
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(r1.getAmount()).transactionType(TransactionType.DISBURSEMENT).description("Disbursement: " + r1.getPurpose()).transactionDate(LocalDateTime.now().minusDays(3)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(r2.getAmount()).transactionType(TransactionType.DISBURSEMENT).description("Disbursement: " + r2.getPurpose()).transactionDate(LocalDateTime.now().minusDays(2)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(r3.getAmount()).transactionType(TransactionType.DISBURSEMENT).description("Disbursement: " + r3.getPurpose()).transactionDate(LocalDateTime.now().minusDays(1)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund).amount(r10.getAmount()).transactionType(TransactionType.DISBURSEMENT).description("Disbursement: " + r10.getPurpose()).transactionDate(LocalDateTime.now().minusDays(6)).build());
    }
}
```

### Task 4: Add Missing Repository Methods

**Files:**
- Modify: `src/main/java/com/khalil/petty_cash_management_system/repository/PettyCashRequestRepository.java`
- Modify: `src/main/java/com/khalil/petty_cash_management_system/repository/DisbursementRepository.java`

- [ ] **Add repository methods needed by the controllers**

```java
// PettyCashRequestRepository.java — add these methods:
java.util.List<PettyCashRequest> findByEmployeeIdOrderByRequestDateDesc(Long employeeId);
java.util.List<PettyCashRequest> findByStatus(RequestStatus status);
```

### Task 5: Login Page

**Files:**
- Create: `src/main/resources/templates/login.html`

- [ ] **Create login page with clean UI**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login - Petty Cash Management</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-slate-50 min-h-screen flex items-center justify-center">
    <div class="w-full max-w-md mx-4">
        <!-- Logo Area -->
        <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-16 h-16 bg-slate-900 text-white rounded-2xl mb-4 shadow-lg">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
            </div>
            <h1 class="text-2xl font-bold text-slate-800">Petty Cash Management</h1>
            <p class="text-slate-500 mt-1">Sign in to your account</p>
        </div>

        <!-- Login Card -->
        <div class="bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
            <div th:if="${error}" class="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                <span th:text="${error}">Invalid credentials</span>
            </div>
            <div th:if="${logout}" class="mb-4 bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg text-sm">
                <span th:text="${logout}">You have been logged out</span>
            </div>

            <form action="/login" method="post" class="space-y-5">
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">Email</label>
                    <input type="email" name="username" required
                           class="w-full px-4 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
                           placeholder="admin@pettycash.com">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">Password</label>
                    <input type="password" name="password" required
                           class="w-full px-4 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition"
                           placeholder="password123">
                </div>
                <button type="submit"
                        class="w-full bg-slate-900 hover:bg-slate-800 text-white py-2.5 rounded-lg font-medium transition shadow-sm">
                    Sign In
                </button>
            </form>

            <!-- Demo Credentials -->
            <div class="mt-6 pt-6 border-t border-slate-100">
                <p class="text-xs text-slate-400 font-medium uppercase tracking-wider mb-3">Demo Accounts</p>
                <div class="space-y-2 text-xs text-slate-500">
                    <div class="flex justify-between"><span>Admin</span><span class="font-mono">admin@pettycash.com</span></div>
                    <div class="flex justify-between"><span>Manager</span><span class="font-mono">manager@pettycash.com</span></div>
                    <div class="flex justify-between"><span>Employee</span><span class="font-mono">eve@pettycash.com</span></div>
                    <div class="flex justify-between pt-1"><span class="text-slate-400">Password for all:</span><span class="font-mono text-slate-400">password123</span></div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

### Task 6: Unified Sidebar

**Files:**
- Modify: `src/main/resources/templates/fragments/sidebar.html`

- [ ] **Replace sidebar with unified version showing user info, role badge, active page, and logout**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<body>

<div th:fragment="sidebar"
     class="w-64 h-screen bg-slate-900 text-white fixed left-0 top-0 flex flex-col z-50">

    <!-- Logo / Brand -->
    <div class="p-5 border-b border-slate-700/50">
        <div class="flex items-center gap-3">
            <div class="w-9 h-9 bg-indigo-500 rounded-lg flex items-center justify-center">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
            </div>
            <div>
                <h1 class="text-lg font-bold">Petty Cash</h1>
                <p class="text-xs text-slate-400">Management System</p>
            </div>
        </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 p-4 space-y-1 overflow-y-auto">
        <a href="/dashboard"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/dashboard') or #strings.startsWith(#httpServletRequest.requestURI, '/admin/dashboard') or #strings.startsWith(#httpServletRequest.requestURI, '/manager/dashboard') or #httpServletRequest.requestURI == '/'} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
            Dashboard
        </a>

        <a href="/requests"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/requests') or #strings.startsWith(#httpServletRequest.requestURI, '/employee/requests')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
            Requests
        </a>

        <a href="/approvals" sec:authorize="hasRole('MANAGER') or hasRole('ADMIN')"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/approvals')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            Approvals
        </a>

        <a href="/disbursements" sec:authorize="hasRole('ADMIN')"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/disbursements')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            Disbursements
        </a>

        <a href="/funds" sec:authorize="hasRole('ADMIN')"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/funds')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            Funds
        </a>

        <a href="/reports" sec:authorize="hasRole('MANAGER') or hasRole('ADMIN')"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/reports')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
            Reports
        </a>

        <a href="/transactions"
           th:classappend="${#strings.startsWith(#httpServletRequest.requestURI, '/transactions')} ? 'bg-slate-700/50 text-white' : 'text-slate-300 hover:bg-slate-700/30 hover:text-white'"
           class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path></svg>
            Transactions
        </a>
    </nav>

    <!-- User Info + Logout -->
    <div class="p-4 border-t border-slate-700/50" sec:authorize="isAuthenticated()">
        <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 rounded-full bg-indigo-500 flex items-center justify-center text-white text-sm font-bold shadow" sec:authentication="principal.username">
                A
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-white truncate" sec:authentication="name">user</p>
                <p class="text-xs text-slate-400 truncate" sec:authentication="principal.authorities">role</p>
            </div>
        </div>
        <a href="/logout"
           class="flex items-center gap-2 px-3 py-2 text-slate-400 hover:text-white hover:bg-slate-700/30 rounded-lg text-sm transition">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
            Sign Out
        </a>
    </div>
</div>

</body>
</html>
```

### Task 7: Admin Dashboard — Dynamic Table

**Files:**
- Modify: `src/main/resources/templates/dashboard-admin.html`

- [ ] **Replace hardcoded table rows with Thymeleaf loop over ${recentDisbursements}**
- [ ] **Add empty state handling for the table**

Key changes from current: Replace lines 108-133 with dynamic loop:

```html
<tbody class="divide-y divide-slate-100">
    <tr th:each="d : ${recentDisbursements}" class="hover:bg-slate-50 transition">
        <td class="px-6 py-4 text-sm text-slate-500" th:text="${#temporals.format(d.disbursementDate, 'MMM dd, yyyy')}">Oct 24</td>
        <td class="px-6 py-4 text-sm font-medium text-slate-800" th:text="${d.request.employee.fullName}">Employee</td>
        <td class="px-6 py-4 text-sm text-slate-500" th:text="${d.request.purpose}">Purpose</td>
        <td class="px-6 py-4 text-sm font-bold text-slate-800 text-right" th:text="'$' + ${d.amount}">$120</td>
        <td class="px-6 py-4 text-center">
            <span class="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-xs font-semibold">Disbursed</span>
        </td>
    </tr>
    <tr th:if="${#lists.isEmpty(recentDisbursements)}">
        <td colspan="5" class="px-6 py-10 text-center text-slate-400">No disbursements yet.</td>
    </tr>
</tbody>
```

### Task 8: Manager Dashboard — Fix Employee Name

**Files:**
- Modify: `src/main/resources/templates/dashboard-manager.html`

- [ ] **Fix employee name reference from firstName+lastName to fullName**

Line 114 currently has: 
```
${req.employee != null ? req.employee.firstName + ' ' + req.employee.lastName : 'Unknown'}
```
Change to:
```
${req.employee != null ? req.employee.fullName : 'Unknown'}
```

### Task 9: Requests Page — Full Table + New Request Form

**Files:**
- Modify: `src/main/resources/templates/requests.html`

- [ ] **Replace with full-featured requests page**

Rewrite completely with:
- Full table: Request#, Date, Employee, Amount, Purpose, Status
- Color-coded status badges
- New Request form (shown only for EMPLOYEE role)
- Empty state
- Consistent styling (slate theme, Inter font)

### Task 10: Approvals Page — Fixed + Actions

**Files:**
- Modify: `src/main/resources/templates/approvals.html`

- [ ] **Fix column count and add approve/reject forms**

Fix: 5 `<th>` headers need 5 `<td>` columns
- Request #, Employee, Decision, Manager, Date
- Add color badges for decision (green=APPROVE, red=REJECT)

### Task 11: Disbursements Page — Full + Form

**Files:**
- Modify: `src/main/resources/templates/disbursements.html`

- [ ] **Fix table + add disbursement form**

Table: Request#, Employee, Amount, Reference#, Accountant, Date
Disbursement Form: Select request dropdown + submit button (for ADMIN only)

### Task 12: Funds Page — Replenishment + Transactions

**Files:**
- Modify: `src/main/resources/templates/funds.html`

- [ ] **Add fund balance display, replenishment form, and transaction history**

### Task 13: Reports Page — Dynamic Data

**Files:**
- Modify: `src/main/resources/templates/reports.html`

- [ ] **Replace hardcoded numbers with Thymeleaf model attributes**

```html
<p class="text-4xl font-bold mt-3" th:text="${totalRequests}">25</p>
<p class="text-4xl font-bold mt-3" th:text="${approvedCount}">20</p>
<p class="text-4xl font-bold mt-3" th:text="'$' + ${fundBalance}">5,000</p>
```

### Task 14: Transactions Page — New Page

**Files:**
- Create: `src/main/resources/templates/transactions.html`

- [ ] **Create transaction history page with full table**
- [ ] **Color-coded transaction types (green=replenishment, red=disbursement)**

### Task 15: Users Page — User Management

**Files:**
- Create: `src/main/resources/templates/users.html`

- [ ] **Create user management page for admin**

### Task 16: Build, Test, and Polish

- [ ] **Build and run the application**
- [ ] **Test all workflows end-to-end**
- [ ] **Fix any issues found during testing**
