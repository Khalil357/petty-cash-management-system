package com.khalil.petty_cash_management_system.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
}