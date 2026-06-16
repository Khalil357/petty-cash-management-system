package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.CreateRequestDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashRequest;
import com.khalil.petty_cash_management_system.entity.User;
import com.khalil.petty_cash_management_system.repository.PettyCashRequestRepository;
import com.khalil.petty_cash_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PettyCashRequestServiceImpl implements PettyCashRequestService {

    private final PettyCashRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public PettyCashRequest createRequest(CreateRequestDTO dto) {

        User employee = userRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PettyCashRequest request = PettyCashRequest.builder()
                .employee(employee)
                .amount(dto.getAmount())
                .purpose(dto.getPurpose())
                .build();

        return requestRepository.save(request);
    }

    @Override
    public List<PettyCashRequest> getAllRequests() {
        return requestRepository.findAll();
    }
}