package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.CreateRequestDTO;
import com.khalil.petty_cash_management_system.entity.PettyCashRequest;
import com.khalil.petty_cash_management_system.service.PettyCashRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class PettyCashRequestController {

    private final PettyCashRequestService service;

    @PostMapping
    public PettyCashRequest createRequest(
            @RequestBody CreateRequestDTO dto
    ) {
        return service.createRequest(dto);
    }

    @GetMapping
    public List<PettyCashRequest> getAllRequests() {
        return service.getAllRequests();
    }
}