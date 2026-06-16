package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.CreateUserRequest;
import com.khalil.petty_cash_management_system.entity.User;

public interface UserService {

    User createUser(CreateUserRequest request);

}