package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {
}