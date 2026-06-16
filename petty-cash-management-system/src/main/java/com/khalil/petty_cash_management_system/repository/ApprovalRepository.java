package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}