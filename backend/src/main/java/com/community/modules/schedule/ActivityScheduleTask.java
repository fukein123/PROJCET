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
        long startedAt = System.currentTimeMillis();
        log.info("Scheduled task started, task=markExpiredActivities");
        try {
            int updated = activityService.endExpiredActivities();
            log.info(
                    "Scheduled task finished, task=markExpiredActivities, updatedRows={}, durationMs={}",
                    updated,
                    System.currentTimeMillis() - startedAt
            );
        } catch (RuntimeException ex) {
            log.error(
                    "Scheduled task failed, task=markExpiredActivities, durationMs={}",
                    System.currentTimeMillis() - startedAt,
                    ex
            );
            throw ex;
        }
    }

    @Scheduled(cron = "0 0 1 ? * MON")
    public void weeklyVolunteerRankingJob() {
        LocalDate requestedWeekStart = LocalDate.now().minusWeeks(1);
        LocalDate weekStart = requestedWeekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long startedAt = System.currentTimeMillis();
        log.info(
                "Scheduled task started, task=weeklyVolunteerRankingJob, requestedWeekStart={}, normalizedWeekStart={}",
                requestedWeekStart,
                weekStart
        );
        try {
            int generated = volunteerWeeklyStatsService.rebuildWeeklyStats(weekStart, "scheduled-cron");
            log.info(
                    "Scheduled task finished, task=weeklyVolunteerRankingJob, normalizedWeekStart={}, generatedRows={}, durationMs={}",
                    weekStart,
                    generated,
                    System.currentTimeMillis() - startedAt
            );
        } catch (RuntimeException ex) {
            log.error(
                    "Scheduled task failed, task=weeklyVolunteerRankingJob, normalizedWeekStart={}, durationMs={}",
                    weekStart,
                    System.currentTimeMillis() - startedAt,
                    ex
            );
            throw ex;
        }
    }
}
