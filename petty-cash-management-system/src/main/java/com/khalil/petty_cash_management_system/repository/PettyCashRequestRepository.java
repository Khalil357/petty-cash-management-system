package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.PettyCashRequest;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PettyCashRequestRepository
        extends JpaRepository<PettyCashRequest, Long> {

    long countByStatus(RequestStatus status);

    long countByStatusAndEmployeeId(RequestStatus status, Long employeeId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.amount), 0) FROM PettyCashRequest p WHERE p.status = :status")
    java.math.BigDecimal sumAmountByStatus(@org.springframework.data.repository.query.Param("status") RequestStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.amount), 0) FROM PettyCashRequest p WHERE p.status = :status AND p.employee.id = :employeeId")
    java.math.BigDecimal sumAmountByStatusAndEmployeeId(@org.springframework.data.repository.query.Param("status") RequestStatus status, @org.springframework.data.repository.query.Param("employeeId") Long employeeId);

    java.util.List<PettyCashRequest> findTop5ByStatusOrderByRequestDateAsc(RequestStatus status);

    java.util.List<PettyCashRequest> findTop5ByEmployeeIdOrderByRequestDateDesc(Long employeeId);
}