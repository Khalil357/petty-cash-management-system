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
        if (roleRepository.count() > 0) {
            return; // already initialized
        }

        // ─── 1. Roles ────────────────────────────────────────────
        Role adminRole = roleRepository.save(
                Role.builder().name(RoleName.ADMIN).description("Administrator").build());
        Role managerRole = roleRepository.save(
                Role.builder().name(RoleName.MANAGER).description("Manager").build());
        Role employeeRole = roleRepository.save(
                Role.builder().name(RoleName.EMPLOYEE).description("Employee").build());

        String pw = passwordEncoder.encode("password123");

        // ─── 2. Users ────────────────────────────────────────────
        User alice = userRepository.save(User.builder()
                .fullName("Alice Admin").email("admin@pettycash.com").password(pw)
                .phone("+1-555-0100").role(adminRole).active(true).build());
        User bob = userRepository.save(User.builder()
                .fullName("Bob Admin").email("bob@pettycash.com").password(pw)
                .phone("+1-555-0101").role(adminRole).active(true).build());
        User carol = userRepository.save(User.builder()
                .fullName("Carol Manager").email("manager@pettycash.com").password(pw)
                .phone("+1-555-0102").role(managerRole).active(true).build());
        User david = userRepository.save(User.builder()
                .fullName("David Manager").email("david@pettycash.com").password(pw)
                .phone("+1-555-0103").role(managerRole).active(true).build());
        User eve = userRepository.save(User.builder()
                .fullName("Eve Employee").email("eve@pettycash.com").password(pw)
                .phone("+1-555-0104").role(employeeRole).active(true).build());
        User frank = userRepository.save(User.builder()
                .fullName("Frank Employee").email("frank@pettycash.com").password(pw)
                .phone("+1-555-0105").role(employeeRole).active(true).build());
        User grace = userRepository.save(User.builder()
                .fullName("Grace Employee").email("grace@pettycash.com").password(pw)
                .phone("+1-555-0106").role(employeeRole).active(true).build());

        // ─── 3. Petty Cash Fund ─────────────────────────────────
        PettyCashFund fund = fundRepository.save(PettyCashFund.builder()
                .currentBalance(new BigDecimal("10000.00"))
                .lastUpdated(LocalDateTime.now())
                .build());

        // ─── 4. Fund Transactions (initial replenishments) ──────
        transactionRepository.save(FundTransaction.builder()
                .fund(fund).amount(new BigDecimal("5000.00"))
                .transactionType(TransactionType.REPLENISHMENT)
                .description("Initial fund allocation")
                .transactionDate(LocalDateTime.now().minusDays(30))
                .build());
        transactionRepository.save(FundTransaction.builder()
                .fund(fund).amount(new BigDecimal("5000.00"))
                .transactionType(TransactionType.REPLENISHMENT)
                .description("Additional fund top-up")
                .transactionDate(LocalDateTime.now().minusDays(15))
                .build());

        // ─── 5. Petty Cash Requests ─────────────────────────────
        PettyCashRequest r1 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-001").amount(new BigDecimal("150.00"))
                .purpose("Office supplies - printer ink and paper")
                .employee(eve).requestDate(LocalDate.now().minusDays(5))
                .status(RequestStatus.DISBURSED).build());
        PettyCashRequest r2 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-002").amount(new BigDecimal("85.50"))
                .purpose("Client lunch meeting")
                .employee(frank).requestDate(LocalDate.now().minusDays(4))
                .status(RequestStatus.DISBURSED).build());
        PettyCashRequest r3 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-003").amount(new BigDecimal("200.00"))
                .purpose("Fuel reimbursement - business trip")
                .employee(eve).requestDate(LocalDate.now().minusDays(3))
                .status(RequestStatus.DISBURSED).build());
        PettyCashRequest r4 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-004").amount(new BigDecimal("45.00"))
                .purpose("Taxi fare to client site")
                .employee(grace).requestDate(LocalDate.now().minusDays(2))
                .status(RequestStatus.APPROVED).build());
        PettyCashRequest r5 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-005").amount(new BigDecimal("320.00"))
                .purpose("Team building event supplies")
                .employee(frank).requestDate(LocalDate.now().minusDays(1))
                .status(RequestStatus.APPROVED).build());
        PettyCashRequest r6 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-006").amount(new BigDecimal("75.00"))
                .purpose("Stationery for monthly report")
                .employee(grace).requestDate(LocalDate.now())
                .status(RequestStatus.PENDING).build());
        PettyCashRequest r7 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-007").amount(new BigDecimal("120.00"))
                .purpose("Software license renewal")
                .employee(eve).requestDate(LocalDate.now())
                .status(RequestStatus.PENDING).build());
        PettyCashRequest r8 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-008").amount(new BigDecimal("50.00"))
                .purpose("Team breakfast meeting")
                .employee(frank).requestDate(LocalDate.now().minusDays(6))
                .status(RequestStatus.REJECTED).build());
        PettyCashRequest r9 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-009").amount(new BigDecimal("180.00"))
                .purpose("Conference registration fee")
                .employee(grace).requestDate(LocalDate.now().minusDays(7))
                .status(RequestStatus.REJECTED).build());
        PettyCashRequest r10 = requestRepository.save(PettyCashRequest.builder()
                .requestNumber("REQ-010").amount(new BigDecimal("95.00"))
                .purpose("Books and reference materials")
                .employee(eve).requestDate(LocalDate.now().minusDays(8))
                .status(RequestStatus.DISBURSED).build());

        // ─── 6. Approvals ───────────────────────────────────────
        approvalRepository.save(Approval.builder().request(r1).manager(carol)
                .decision(Decision.APPROVE).comments("Approved for office use")
                .approvalDate(LocalDateTime.now().minusDays(4)).build());
        approvalRepository.save(Approval.builder().request(r2).manager(carol)
                .decision(Decision.APPROVE).comments("Client entertainment approved")
                .approvalDate(LocalDateTime.now().minusDays(3)).build());
        approvalRepository.save(Approval.builder().request(r3).manager(carol)
                .decision(Decision.APPROVE).comments("Business travel approved")
                .approvalDate(LocalDateTime.now().minusDays(2)).build());
        approvalRepository.save(Approval.builder().request(r4).manager(david)
                .decision(Decision.APPROVE).comments("Transport approved")
                .approvalDate(LocalDateTime.now().minusDays(1)).build());
        approvalRepository.save(Approval.builder().request(r5).manager(david)
                .decision(Decision.APPROVE).comments("Team event approved")
                .approvalDate(LocalDateTime.now()).build());
        approvalRepository.save(Approval.builder().request(r8).manager(carol)
                .decision(Decision.REJECT).comments("Budget exceeded for this category")
                .approvalDate(LocalDateTime.now().minusDays(5)).build());
        approvalRepository.save(Approval.builder().request(r9).manager(david)
                .decision(Decision.REJECT).comments("Already covered by department budget")
                .approvalDate(LocalDateTime.now().minusDays(6)).build());
        approvalRepository.save(Approval.builder().request(r10).manager(carol)
                .decision(Decision.APPROVE).comments("Educational materials approved")
                .approvalDate(LocalDateTime.now().minusDays(7)).build());

        // ─── 7. Disbursements ───────────────────────────────────
        disbursementRepository.save(Disbursement.builder().request(r1).accountant(alice)
                .amount(r1.getAmount()).referenceNumber("DISB-A1B2C3D4")
                .disbursementDate(LocalDateTime.now().minusDays(3)).build());
        disbursementRepository.save(Disbursement.builder().request(r2).accountant(alice)
                .amount(r2.getAmount()).referenceNumber("DISB-E5F6G7H8")
                .disbursementDate(LocalDateTime.now().minusDays(2)).build());
        disbursementRepository.save(Disbursement.builder().request(r3).accountant(alice)
                .amount(r3.getAmount()).referenceNumber("DISB-I9J0K1L2")
                .disbursementDate(LocalDateTime.now().minusDays(1)).build());
        disbursementRepository.save(Disbursement.builder().request(r10).accountant(bob)
                .amount(r10.getAmount()).referenceNumber("DISB-M3N4O5P6")
                .disbursementDate(LocalDateTime.now().minusDays(6)).build());

        // ─── 8. Fund Transactions (disbursements) ───────────────
        transactionRepository.save(FundTransaction.builder().fund(fund)
                .amount(r1.getAmount()).transactionType(TransactionType.DISBURSEMENT)
                .description("Disbursement: " + r1.getPurpose())
                .transactionDate(LocalDateTime.now().minusDays(3)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund)
                .amount(r2.getAmount()).transactionType(TransactionType.DISBURSEMENT)
                .description("Disbursement: " + r2.getPurpose())
                .transactionDate(LocalDateTime.now().minusDays(2)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund)
                .amount(r3.getAmount()).transactionType(TransactionType.DISBURSEMENT)
                .description("Disbursement: " + r3.getPurpose())
                .transactionDate(LocalDateTime.now().minusDays(1)).build());
        transactionRepository.save(FundTransaction.builder().fund(fund)
                .amount(r10.getAmount()).transactionType(TransactionType.DISBURSEMENT)
                .description("Disbursement: " + r10.getPurpose())
                .transactionDate(LocalDateTime.now().minusDays(6)).build());
    }
}
