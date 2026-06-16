package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.FundReplenishmentDTO;
import com.khalil.petty_cash_management_system.entity.FundTransaction;
import com.khalil.petty_cash_management_system.entity.PettyCashFund;
import com.khalil.petty_cash_management_system.enums.TransactionType;
import com.khalil.petty_cash_management_system.repository.FundTransactionRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashFundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService {

    private final PettyCashFundRepository fundRepository;
    private final FundTransactionRepository transactionRepository;

    @Override
    public PettyCashFund replenishFund(FundReplenishmentDTO dto) {

        PettyCashFund fund = fundRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("Fund not found"));

        fund.setCurrentBalance(
                fund.getCurrentBalance().add(dto.getAmount())
        );

        fundRepository.save(fund);

        FundTransaction transaction =
                FundTransaction.builder()
                        .fund(fund)
                        .amount(dto.getAmount())
                        .description(dto.getDescription())
                        .transactionType(TransactionType.REPLENISHMENT)
                        .build();

        transactionRepository.save(transaction);

        return fund;
    }
}