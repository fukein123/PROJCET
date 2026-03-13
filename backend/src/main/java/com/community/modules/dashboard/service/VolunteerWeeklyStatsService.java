package com.community.modules.dashboard.service;

import com.community.modules.dashboard.dto.VolunteerWeeklyRankingItem;
import com.community.modules.dashboard.dto.VolunteerWeeklyRankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerWeeklyStatsService {

    private static final int MAX_PAGE_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    public VolunteerWeeklyRankingResponse queryWeeklyRanking(LocalDate weekStart, int size) {
        LocalDate targetWeekStart = normalizeWeekStart(weekStart == null ? LocalDate.now() : weekStart);
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        ensureWeekStatsExists(targetWeekStart);

        Date weekDate = Date.valueOf(targetWeekStart);
        Integer total = countWeekStats(weekDate);
        LocalDateTime generatedTime = jdbcTemplate.queryForObject(
                "select max(generated_time) from volunteer_weekly_stats where week_start = ?",
                LocalDateTime.class,
                weekDate
        );

        List<VolunteerWeeklyRankingItem> records = jdbcTemplate.query("""
                        select rank_no, user_id, username, real_name, completed_count, sign_in_count, sign_out_count, service_minutes
                        from volunteer_weekly_stats
                        where week_start = ?
                        order by rank_no asc
                        limit ?
                        """,
                (rs, rowNum) -> {
                    int serviceMinutes = rs.getInt("service_minutes");
                    BigDecimal serviceHours = BigDecimal.valueOf(serviceMinutes)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    return new VolunteerWeeklyRankingItem(
                            rs.getInt("rank_no"),
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("real_name"),
                            rs.getInt("completed_count"),
                            rs.getInt("sign_in_count"),
                            rs.getInt("sign_out_count"),
                            serviceMinutes,
                            serviceHours.stripTrailingZeros().toPlainString()
                    );
                },
                weekDate,
                limit
        );

        return new VolunteerWeeklyRankingResponse(
                targetWeekStart,
                targetWeekStart.plusDays(6),
                generatedTime,
                safeCount(total),
                records
        );
    }

    public int rebuildWeeklyStats(LocalDate weekStart) {
        return rebuildWeeklyStats(weekStart, "direct-call");
    }

    @Transactional(rollbackFor = Exception.class)
    public int rebuildWeeklyStats(LocalDate weekStart, String triggerSource) {
        LocalDate requestedWeekStart = weekStart == null ? LocalDate.now().minusWeeks(1) : weekStart;
        LocalDate targetWeekStart = normalizeWeekStart(
                requestedWeekStart
        );
        LocalDateTime weekFrom = targetWeekStart.atStartOfDay();
        LocalDateTime weekTo = targetWeekStart.plusWeeks(1).atStartOfDay();
        Date weekDate = Date.valueOf(targetWeekStart);
        Timestamp weekFromTs = Timestamp.valueOf(weekFrom);
        Timestamp weekToTs = Timestamp.valueOf(weekTo);
        String trigger = (triggerSource == null || triggerSource.isBlank()) ? "unspecified" : triggerSource;
        Integer existingRows = countWeekStats(weekDate);
        long startedAt = System.currentTimeMillis();

        log.info(
                "Weekly ranking rebuild started, trigger={}, requestedWeekStart={}, normalizedWeekStart={}, existingRows={}, weekFrom={}, weekTo={}",
                trigger,
                requestedWeekStart,
                targetWeekStart,
                safeCount(existingRows),
                weekFrom,
                weekTo
        );

        try {
            int deletedRows = jdbcTemplate.update("delete from volunteer_weekly_stats where week_start = ?", weekDate);

            int insertedRows = jdbcTemplate.update("""
                            insert into volunteer_weekly_stats (
                                week_start,
                                user_id,
                                username,
                                real_name,
                                completed_count,
                                sign_in_count,
                                sign_out_count,
                                service_minutes,
                                rank_no,
                                generated_time,
                                create_time,
                                update_time
                            )
                            select
                                ? as week_start,
                                t.user_id,
                                t.username,
                                t.real_name,
                                t.completed_count,
                                t.sign_in_count,
                                t.sign_out_count,
                                t.service_minutes,
                                row_number() over (
                                    order by t.service_minutes desc, t.completed_count desc, t.sign_out_count desc, t.user_id asc
                                ) as rank_no,
                                now(),
                                now(),
                                now()
                            from (
                                select
                                    u.id as user_id,
                                    u.username,
                                    u.real_name,
                                    coalesce(sum(case when acr.sign_out_time >= ? and acr.sign_out_time < ? then 1 else 0 end), 0) as completed_count,
                                    coalesce(sum(case when acr.sign_in_time >= ? and acr.sign_in_time < ? then 1 else 0 end), 0) as sign_in_count,
                                    coalesce(sum(case when acr.sign_out_time >= ? and acr.sign_out_time < ? then 1 else 0 end), 0) as sign_out_count,
                                    coalesce(sum(case
                                        when acr.sign_in_time is not null
                                          and acr.sign_out_time is not null
                                          and acr.sign_out_time >= ?
                                          and acr.sign_out_time < ?
                                        then greatest(timestampdiff(minute, acr.sign_in_time, acr.sign_out_time), 0)
                                        else 0
                                    end), 0) as service_minutes
                                from sys_user u
                                left join activity_check_record acr on acr.user_id = u.id
                                where u.role = 'VOLUNTEER' and u.status = 1
                                group by u.id, u.username, u.real_name
                            ) t
                            where t.sign_in_count > 0
                               or t.sign_out_count > 0
                               or t.completed_count > 0
                               or t.service_minutes > 0
                            """,
                    weekDate,
                    weekFromTs, weekToTs,
                    weekFromTs, weekToTs,
                    weekFromTs, weekToTs,
                    weekFromTs, weekToTs
            );

            log.info(
                    "Weekly ranking rebuild finished, trigger={}, normalizedWeekStart={}, deletedRows={}, insertedRows={}, durationMs={}",
                    trigger,
                    targetWeekStart,
                    deletedRows,
                    insertedRows,
                    System.currentTimeMillis() - startedAt
            );
            return insertedRows;
        } catch (RuntimeException ex) {
            log.error(
                    "Weekly ranking rebuild failed, trigger={}, requestedWeekStart={}, normalizedWeekStart={}, existingRows={}, durationMs={}",
                    trigger,
                    requestedWeekStart,
                    targetWeekStart,
                    safeCount(existingRows),
                    System.currentTimeMillis() - startedAt,
                    ex
            );
            throw ex;
        }
    }

    private void ensureWeekStatsExists(LocalDate weekStart) {
        Date weekDate = Date.valueOf(weekStart);
        Integer count = countWeekStats(weekDate);
        if (count == null || count == 0) {
            log.info("Weekly ranking snapshot missing, weekStart={}, trigger=query-auto-rebuild", weekStart);
            rebuildWeeklyStats(weekStart, "query-auto-rebuild");
        }
    }

    private LocalDate normalizeWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Integer countWeekStats(Date weekDate) {
        return jdbcTemplate.queryForObject(
                "select count(1) from volunteer_weekly_stats where week_start = ?",
                Integer.class,
                weekDate
        );
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }
}
