package com.community.modules.activity.controller;

import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.activity.dto.ActivityCategoryRequest;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.ActivityCategory;
import com.community.modules.activity.entity.ActivityCheckRecord;
import com.community.modules.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "List all activity categories")
    @GetMapping("/categories")
    public ApiResponse<List<ActivityCategory>> categories() {
        return ApiResponse.success(activityService.listCategories());
    }

    @Operation(summary = "Admin - create category")
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createCategory(@Valid @RequestBody ActivityCategoryRequest request) {
        activityService.createCategory(request);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update category")
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody ActivityCategoryRequest request) {
        activityService.updateCategory(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Page activities")
    @GetMapping("/page")
    public ApiResponse<PageResult<Activity>> page(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String status) {
        return ApiResponse.success(activityService.pageActivities(current, size, keyword, categoryId, status));
    }

    @Operation(summary = "Activity detail")
    @GetMapping("/{id}")
    public ApiResponse<Activity> detail(@PathVariable Long id) {
        return ApiResponse.success(activityService.detail(id));
    }

    @Operation(summary = "Admin - create activity")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> create(@Valid @RequestBody ActivityRequest request) {
        activityService.createActivity(request);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - update activity")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        activityService.updateActivity(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Volunteer - apply for activity")
    @PostMapping("/{activityId}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> apply(@PathVariable Long activityId) {
        activityService.applyForActivity(activityId);
        return ApiResponse.success("applied", null);
    }

    @Operation(summary = "Admin - page all applications")
    @GetMapping("/applications/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<ActivityApplication>> pageApplications(@RequestParam(defaultValue = "1") long current,
                                                                         @RequestParam(defaultValue = "10") long size,
                                                                         @RequestParam(required = false) Long activityId,
                                                                         @RequestParam(required = false) String status) {
        return ApiResponse.success(activityService.pageApplications(current, size, activityId, status, false));
    }

    @Operation(summary = "Volunteer - my applications")
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResult<ActivityApplication>> myApplications(@RequestParam(defaultValue = "1") long current,
                                                                       @RequestParam(defaultValue = "10") long size,
                                                                       @RequestParam(required = false) Long activityId,
                                                                       @RequestParam(required = false) String status) {
        return ApiResponse.success(activityService.pageApplications(current, size, activityId, status, true));
    }

    @Operation(summary = "Admin - audit application")
    @PutMapping("/applications/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> audit(@PathVariable Long id, @Valid @RequestBody ApplicationAuditRequest request) {
        activityService.auditApplication(id, request);
        return ApiResponse.success("audited", null);
    }

    @Operation(summary = "Volunteer - sign in")
    @PostMapping("/sign/in")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> signIn(@Valid @RequestBody SignRequest request) {
        activityService.signIn(request);
        return ApiResponse.success("sign-in success", null);
    }

    @Operation(summary = "Volunteer - sign out")
    @PostMapping("/sign/out")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> signOut(@Valid @RequestBody SignRequest request) {
        activityService.signOut(request);
        return ApiResponse.success("sign-out success", null);
    }

    @Operation(summary = "Volunteer - my sign records")
    @GetMapping("/sign/my-records")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResult<ActivityCheckRecord>> myRecords(@RequestParam(defaultValue = "1") long current,
                                                                  @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(activityService.myCheckRecords(current, size));
    }
}

