package com.community.modules.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckRecordView {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private String activityAddress;
    private Long userId;
    private String username;
    private String realName;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private LocalDateTime signInTime;
    private LocalDateTime signOutTime;
    private Double signInDistance;
    private Double signOutDistance;
    private String status;
    private Long serviceMinutes;
}
