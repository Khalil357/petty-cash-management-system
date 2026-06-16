package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.FundReplenishmentDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashFund;

public interface FundService {

    PettyCashFund replenishFund(FundReplenishmentDTO dto);
}