package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.DashboardDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashFund;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.repository.PettyCashFundRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashRequestRepository;
import com.khalil.petty_cash_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final PettyCashRequestRepository requestRepository;
    private final PettyCashFundRepository fundRepository;

    @Override
    public DashboardDTO getDashboard() {

        PettyCashFund fund = fundRepository.findById(1L).orElse(null);

        return DashboardDTO.builder()
                .totalUsers(userRepository.count())
                .totalRequests(requestRepository.count())
                .pendingRequests(
                        requestRepository.countByStatus(RequestStatus.PENDING))
                .approvedRequests(
                        requestRepository.countByStatus(RequestStatus.APPROVED))
                .rejectedRequests(
                        requestRepository.countByStatus(RequestStatus.REJECTED))
                .disbursedRequests(
                        requestRepository.countByStatus(RequestStatus.DISBURSED))
                .currentFundBalance(
                        fund != null ? fund.getCurrentBalance() : null)
                .build();
    }

    @Override
    public java.util.Map<String, Object> getDashboardStats() {
        DashboardDTO dto = getDashboard();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("userCount", dto.getTotalUsers());
        stats.put("pendingCount", dto.getPendingRequests());
        stats.put("approvedCount", dto.getApprovedRequests());
        stats.put("fundBalance", dto.getCurrentFundBalance() != null ? dto.getCurrentFundBalance() : java.math.BigDecimal.ZERO);
        return stats;
    }

    @Override
    public java.util.Map<String, Object> getManagerDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pendingApprovalCount", requestRepository.countByStatus(RequestStatus.PENDING));
        stats.put("approvedCount", requestRepository.countByStatus(RequestStatus.APPROVED));
        stats.put("totalApprovedValue", requestRepository.sumAmountByStatus(RequestStatus.APPROVED));
        stats.put("rejectedCount", requestRepository.countByStatus(RequestStatus.REJECTED));
        stats.put("pendingRequests", requestRepository.findTop5ByStatusOrderByRequestDateAsc(RequestStatus.PENDING));
        return stats;
    }

    @Override
    public java.util.Map<String, Object> getEmployeeDashboardStats(Long employeeId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("myPendingCount", requestRepository.countByStatusAndEmployeeId(RequestStatus.PENDING, employeeId));
        stats.put("myApprovedCount", requestRepository.countByStatusAndEmployeeId(RequestStatus.APPROVED, employeeId));
        stats.put("myTotalDisbursed", requestRepository.sumAmountByStatusAndEmployeeId(RequestStatus.DISBURSED, employeeId));
        stats.put("myRecentRequests", requestRepository.findTop5ByEmployeeIdOrderByRequestDateDesc(employeeId));
        return stats;
    }
}