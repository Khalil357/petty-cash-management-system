package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.PettyCashFund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PettyCashFundRepository extends JpaRepository<PettyCashFund, Long> {
}