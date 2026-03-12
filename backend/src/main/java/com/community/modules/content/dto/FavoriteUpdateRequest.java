package com.community.modules.content.dto;

import lombok.Data;

@Data
public class FavoriteUpdateRequest {
    private String note;
    private String tag;
    private Integer priority;
}
