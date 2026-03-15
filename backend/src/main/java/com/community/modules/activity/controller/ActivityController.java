package com.community.modules.activity.controller;

import com.community.common.dto.IdListRequest;
import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.activity.dto.ActivityApplyRequest;
import com.community.modules.activity.dto.ActivityCategoryRequest;
import com.community.modules.activity.dto.ActivityApplicationView;
import com.community.modules.activity.dto.ActivityRequest;
import com.community.modules.activity.dto.ApplicationAuditRequest;
import com.community.modules.activity.dto.CheckRecordView;
import com.community.modules.activity.dto.SignRequest;
import com.community.modules.activity.entity.ActivityApplication;
import com.community.modules.activity.entity.Activity;
import com.community.modules.activity.entity.ActivityCategory;
import com.community.modules.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Admin - delete category")
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        activityService.deleteCategory(id);
        return ApiResponse.success("deleted", null);
    }

    @Operation(summary = "Admin - batch delete categories")
    @PostMapping("/categories/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteCategories(@Valid @RequestBody IdListRequest request) {
        activityService.batchDeleteCategories(request.getIds());
        return ApiResponse.success("batch deleted", null);
    }

    @Operation(summary = "Page activities")
    @GetMapping("/page")
    public ApiResponse<PageResult<Activity>> page(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "false") boolean includeArchived) {
        return ApiResponse.success(activityService.pageActivities(current, size, keyword, categoryId, status, includeArchived));
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

    @Operation(summary = "Admin - archive activity (delete alias)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ApiResponse.success("archived", null);
    }

    @Operation(summary = "Admin - batch archive activities (delete alias)")
    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody IdListRequest request) {
        activityService.batchDeleteActivities(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch archive activities")
    @PostMapping("/batch-archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchArchive(@Valid @RequestBody IdListRequest request) {
        activityService.batchArchiveActivities(request.getIds());
        return ApiResponse.success("batch archived", null);
    }

    @Operation(summary = "Admin - batch restore activities")
    @PostMapping("/batch-restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchRestore(@Valid @RequestBody IdListRequest request) {
        activityService.batchRestoreActivities(request.getIds());
        return ApiResponse.success("batch restored", null);
    }

    @Operation(summary = "Volunteer - apply for activity")
    @PostMapping("/{activityId}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<ActivityApplication> apply(@PathVariable Long activityId, @Valid @RequestBody ActivityApplyRequest request) {
        return ApiResponse.success("applied", activityService.applyForActivity(activityId, request.getApplyReason()));
    }

    @Operation(summary = "Admin - page all applications")
    @GetMapping("/applications/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<ActivityApplicationView>> pageApplications(@RequestParam(defaultValue = "1") long current,
                                                                             @RequestParam(defaultValue = "10") long size,
                                                                             @RequestParam(required = false) Long activityId,
                                                                             @RequestParam(required = false) String status) {
        return ApiResponse.success(activityService.pageApplications(current, size, activityId, status, false));
    }

    @Operation(summary = "Volunteer - my applications")
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<PageResult<ActivityApplicationView>> myApplications(@RequestParam(defaultValue = "1") long current,
                                                                           @RequestParam(defaultValue = "10") long size,
                                                                           @RequestParam(required = false) Long activityId,
                                                                           @RequestParam(required = false) String status) {
        return ApiResponse.success(activityService.pageApplications(current, size, activityId, status, true));
    }

    @Operation(summary = "Volunteer - undo my application")
    @DeleteMapping("/applications/{id}/undo")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> undoApplication(@PathVariable Long id) {
        activityService.undoActivityApplication(id);
        return ApiResponse.success("undone", null);
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
    public ApiResponse<PageResult<CheckRecordView>> myRecords(@RequestParam(defaultValue = "1") long current,
                                                               @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(activityService.myCheckRecords(current, size));
    }

    @Operation(summary = "Admin - page all sign records")
    @GetMapping("/sign/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<CheckRecordView>> pageCheckRecords(@RequestParam(defaultValue = "1") long current,
                                                                     @RequestParam(defaultValue = "10") long size,
                                                                     @RequestParam(required = false) Long activityId,
                                                                     @RequestParam(required = false) Long userId,
                                                                     @RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String keyword) {
        return ApiResponse.success(activityService.pageCheckRecords(current, size, activityId, userId, status, keyword));
    }
}
