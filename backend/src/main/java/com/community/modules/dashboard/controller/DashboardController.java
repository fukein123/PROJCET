package com.community.modules.dashboard.controller;

import com.community.common.web.ApiResponse;
import com.community.modules.dashboard.dto.VolunteerWeeklyRankingResponse;
import com.community.modules.dashboard.service.DashboardService;
import com.community.modules.dashboard.service.VolunteerWeeklyStatsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final VolunteerWeeklyStatsService volunteerWeeklyStatsService;

    @Operation(summary = "Admin dashboard statistics")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> admin() {
        return ApiResponse.success(dashboardService.adminDashboard());
    }

    @Operation(summary = "Weekly volunteer ranking")
    @GetMapping("/weekly-ranking")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTEER')")
    public ApiResponse<VolunteerWeeklyRankingResponse> weeklyRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(volunteerWeeklyStatsService.queryWeeklyRanking(weekStart, size));
    }

    @Operation(summary = "Admin - rebuild weekly volunteer ranking")
    @PostMapping("/weekly-ranking/rebuild")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rebuildWeeklyRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        int generated = volunteerWeeklyStatsService.rebuildWeeklyStats(weekStart, "manual-api");
        return ApiResponse.success("rebuild success, rows=" + generated, null);
    }
}
