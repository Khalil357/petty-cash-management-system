package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.ApprovalRequestDTO;
import com.khalil.petty_cash_management_system.entity.Approval;
import com.khalil.petty_cash_management_system.entity.PettyCashRequest;
import com.khalil.petty_cash_management_system.entity.User;
import com.khalil.petty_cash_management_system.enums.Decision;
import com.khalil.petty_cash_management_system.enums.RequestStatus;
import com.khalil.petty_cash_management_system.repository.ApprovalRepository;
import com.khalil.petty_cash_management_system.repository.PettyCashRequestRepository;
import com.khalil.petty_cash_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final PettyCashRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public Approval approveRequest(ApprovalRequestDTO dto) {

        PettyCashRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User manager = userRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (dto.getDecision() == Decision.APPROVE) {
            request.setStatus(RequestStatus.APPROVED);
        } else {
            request.setStatus(RequestStatus.REJECTED);
        }

        requestRepository.save(request);

        Approval approval = Approval.builder()
                .request(request)
                .manager(manager)
                .decision(dto.getDecision())
                .comments(dto.getComments())
                .build();

        return approvalRepository.save(approval);
    }

    @Override
    public List<Approval> getAllApprovals() {
        return approvalRepository.findAll();
    }
}