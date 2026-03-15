package com.community.modules.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityApplicationView {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Long userId;
    private String username;
    private String realName;
    private String applyReason;
    private String status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private Long auditorId;
}
