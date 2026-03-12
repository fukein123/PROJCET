package com.community.modules.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> adminDashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("activityCount", queryCount("select count(1) from activity"));
        result.put("postCount", queryCount("select count(1) from forum_post"));
        result.put("commentCount", queryCount("select count(1) from comment_info where status=1"));
        result.put("volunteerCount", queryCount("select count(1) from sys_user where role='VOLUNTEER'"));
        result.put("weeklyApplicationTrend", weeklyApplicationTrend());
        result.put("activityTypeBar", activityTypeDistribution());
        result.put("postTypePie", postTypeDistribution());
        return result;
    }

    private long queryCount(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }

    private List<Map<String, Object>> weeklyApplicationTrend() {
        return jdbcTemplate.queryForList("""
                select date_format(apply_time, '%Y-%m-%d') as day, count(1) as value
                from activity_application
                where apply_time >= date_sub(curdate(), interval 6 day)
                group by date_format(apply_time, '%Y-%m-%d')
                order by day
                """);
    }

    private List<Map<String, Object>> activityTypeDistribution() {
        return jdbcTemplate.queryForList("""
                select c.name as name, count(a.id) as value
                from activity_category c
                left join activity a on a.category_id = c.id
                group by c.id, c.name
                order by c.sort asc, c.id asc
                """);
    }

    private List<Map<String, Object>> postTypeDistribution() {
        return jdbcTemplate.queryForList("""
                select c.name as name, count(p.id) as value
                from forum_category c
                left join forum_post p on p.category_id = c.id and p.status='APPROVED'
                group by c.id, c.name
                order by c.sort asc, c.id asc
                """);
    }
}
