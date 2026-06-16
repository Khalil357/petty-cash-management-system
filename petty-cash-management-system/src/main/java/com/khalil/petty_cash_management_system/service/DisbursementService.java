package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.DisbursementRequestDTO;
import com.khalil.petty_cash_management_system.entity.Disbursement;

public interface DisbursementService {

    Disbursement disburseCash(DisbursementRequestDTO dto);
}