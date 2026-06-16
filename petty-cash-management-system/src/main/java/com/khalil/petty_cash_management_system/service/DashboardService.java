package com.khalil.petty_cash_management_system.service;

import com.khalil.petty_cash_management_system.dto.DashboardDTO;
import java.util.Map;

public interface DashboardService {

    DashboardDTO getDashboard();

    Map<String, Object> getDashboardStats();

    Map<String, Object> getManagerDashboardStats();

    Map<String, Object> getEmployeeDashboardStats(Long employeeId);
}