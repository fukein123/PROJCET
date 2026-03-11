package com.community.modules.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record VolunteerWeeklyRankingResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        LocalDateTime generatedTime,
        Integer total,
        List<VolunteerWeeklyRankingItem> records
) {
}
