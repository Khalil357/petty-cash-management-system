package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.CreateRequestDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashRequest;

import java.util.List;

public interface PettyCashRequestService {

    PettyCashRequest createRequest(CreateRequestDTO dto);

    List<PettyCashRequest> getAllRequests();
}