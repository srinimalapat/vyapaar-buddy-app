package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboardStats(Long businessId);
}
