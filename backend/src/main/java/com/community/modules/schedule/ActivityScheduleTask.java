package com.community.modules.schedule;

import com.community.modules.activity.service.ActivityService;
import com.community.modules.dashboard.service.VolunteerWeeklyStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityScheduleTask {

    private final ActivityService activityService;
    private final VolunteerWeeklyStatsService volunteerWeeklyStatsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void markExpiredActivities() {
        int updated = activityService.endExpiredActivities();
        log.info("Scheduled task - marked {} expired activities as ENDED", updated);
    }

    @Scheduled(cron = "0 0 1 ? * MON")
    public void weeklyVolunteerRankingJob() {
        LocalDate weekStart = LocalDate.now()
                .minusWeeks(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int generated = volunteerWeeklyStatsService.rebuildWeeklyStats(weekStart);
        log.info("Scheduled task - rebuilt weekly volunteer ranking for weekStart={}, rows={}", weekStart, generated);
    }
}
