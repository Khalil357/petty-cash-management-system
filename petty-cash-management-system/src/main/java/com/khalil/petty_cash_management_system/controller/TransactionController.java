package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.entity.FundTransaction;
import com.khalil.petty_cash_management_system.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final FundTransactionRepository transactionRepository;

    @GetMapping
    public List<FundTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}