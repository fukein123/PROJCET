package com.community.modules.audit.controller;

import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.audit.entity.AdminOperationLog;
import com.community.modules.audit.service.AdminOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    @Operation(summary = "Admin - paginate operation logs")
    @GetMapping("/operations/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<AdminOperationLog>> pageOperations(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String operatorKeyword,
            @RequestParam(required = false) String targetKeyword) {
        return ApiResponse.success(adminOperationLogService.page(
                current,
                size,
                actionType,
                targetType,
                result,
                operatorKeyword,
                targetKeyword
        ));
    }
}
