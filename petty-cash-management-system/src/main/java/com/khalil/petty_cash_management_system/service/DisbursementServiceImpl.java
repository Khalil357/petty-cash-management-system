package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.DisbursementRequestDTO;
import com.khalil.petty_cash_management_system.entity.*;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.enums.TransactionType;
import com.khalil.petty_cash_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final DisbursementRepository disbursementRepository;
    private final PettyCashRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final PettyCashFundRepository fundRepository;
    private final FundTransactionRepository transactionRepository;

    @Override
    public Disbursement disburseCash(DisbursementRequestDTO dto) {

        PettyCashRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User accountant = userRepository.findById(dto.getAccountantId())
                .orElseThrow(() -> new RuntimeException("Accountant not found"));

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new RuntimeException("Only approved requests can be disbursed");
        }

        PettyCashFund fund = fundRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Petty Cash Fund not found"));

        fund.setCurrentBalance(
                fund.getCurrentBalance().subtract(request.getAmount())
        );

        fundRepository.save(fund);

        FundTransaction transaction = FundTransaction.builder()
                .fund(fund)
                .amount(request.getAmount())
                .transactionType(TransactionType.DISBURSEMENT)
                .description("Petty Cash Disbursement")
                .build();

        transactionRepository.save(transaction);

        request.setStatus(RequestStatus.DISBURSED);
        requestRepository.save(request);

        Disbursement disbursement = Disbursement.builder()
                .request(request)
                .accountant(accountant)
                .amount(request.getAmount())
                .referenceNumber(
                        "DISB-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0,8)
                )
                .build();

        return disbursementRepository.save(disbursement);
    }
}