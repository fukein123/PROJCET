package com.community.modules.dashboard.dto;

public record VolunteerWeeklyRankingItem(
        Integer rankNo,
        Long userId,
        String username,
        String realName,
        Integer completedCount,
        Integer signInCount,
        Integer signOutCount,
        Integer serviceMinutes,
        String serviceHours
) {
}
