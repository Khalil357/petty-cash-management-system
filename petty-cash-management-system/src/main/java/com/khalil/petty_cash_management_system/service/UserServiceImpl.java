package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.CreateUserRequest;
import com.khalil.petty_cash_management_system.entity.Role;
import com.khalil.petty_cash_management_system.entity.User;
import com.khalil.petty_cash_management_system.enums.RoleName;
import com.khalil.petty_cash_management_system.repository.RoleRepository;
import com.khalil.petty_cash_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User createUser(CreateUserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByName(
                RoleName.valueOf(request.getRole().toUpperCase())
        ).orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .role(role)
                .build();

        return userRepository.save(user);
    }
}