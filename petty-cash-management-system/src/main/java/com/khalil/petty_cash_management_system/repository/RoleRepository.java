package com.khalil.petty_cash_management_system.repository;

import com.khalil.petty_cash_management_system.entity.Role;
import com.khalil.petty_cash_management_system.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

}