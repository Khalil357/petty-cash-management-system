package com.khalil.petty_cash_management_system.config;

import com.khalil.petty_cash_management_system.entity.Role;
import com.khalil.petty_cash_management_system.enums.RoleName;
import com.khalil.petty_cash_management_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        if(roleRepository.count() == 0){

            roleRepository.save(
                    Role.builder()
                            .name(RoleName.ADMIN)
                            .description("System Administrator")
                            .build());

            roleRepository.save(
                    Role.builder()
                            .name(RoleName.MANAGER)
                            .description("Manager")
                            .build());

            roleRepository.save(
                    Role.builder()
                            .name(RoleName.EMPLOYEE)
                            .description("Employee")
                            .build());
        }
    }
}