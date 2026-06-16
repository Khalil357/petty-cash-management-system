package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {
}