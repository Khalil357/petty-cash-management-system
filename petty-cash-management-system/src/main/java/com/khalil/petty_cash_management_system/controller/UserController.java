package com.khalil.petty_cash_management_system.controller;

import com.khalil.petty_cash_management_system.dto.CreateUserRequest;
import com.khalil.petty_cash_management_system.entity.User;
import com.khalil.petty_cash_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(
            @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(request);
    }
}