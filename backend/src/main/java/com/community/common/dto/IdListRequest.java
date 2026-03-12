package com.community.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class IdListRequest {
    @NotEmpty(message = "请选择至少一条数据")
    private List<Long> ids;
}
