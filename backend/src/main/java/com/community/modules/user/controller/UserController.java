package com.community.modules.user.controller;

import com.community.common.dto.IdListRequest;
import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.user.dto.PasswordUpdateRequest;
import com.community.modules.user.dto.UserCreateRequest;
import com.community.modules.user.dto.UserUpdateRequest;
import com.community.modules.user.dto.VolunteerCertificationAuditRequest;
import com.community.modules.user.dto.VolunteerCertificationSubmitRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.entity.VolunteerCertification;
import com.community.modules.user.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Admin - paginate users")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<User>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String role,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer certified,
                                              @RequestParam(required = false) String certificationStatus) {
        return ApiResponse.success(userService.pageUsers(
                current,
                size,
                role,
                keyword,
                status,
                certified,
                certificationStatus
        ));
    }

    @Operation(summary = "Admin - user detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    @Operation(summary = "Admin - update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateByAdmin(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.adminUpdateUser(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Admin - create user")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return ApiResponse.success("created", null);
    }

    @Operation(summary = "Admin - disable user")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ApiResponse.success("disabled", null);
    }

    @Operation(summary = "Admin - enable user")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ApiResponse.success("enabled", null);
    }

    @Operation(summary = "Admin - batch disable users")
    @PostMapping("/batch-disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDisableUsers(@Valid @RequestBody IdListRequest request) {
        userService.batchDisableUsers(request.getIds());
        return ApiResponse.success("batch disabled", null);
    }

    @Operation(summary = "Admin - batch enable users")
    @PostMapping("/batch-enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchEnableUsers(@Valid @RequestBody IdListRequest request) {
        userService.batchEnableUsers(request.getIds());
        return ApiResponse.success("batch enabled", null);
    }

    @Operation(summary = "Admin - delete volunteer")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteVolunteer(id);
        return ApiResponse.success("deleted", null);
    }

    @Operation(summary = "Admin - batch delete volunteers")
    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteUser(@Valid @RequestBody IdListRequest request) {
        userService.batchDeleteVolunteers(request.getIds());
        return ApiResponse.success("batch deleted", null);
    }

    @Operation(summary = "Current user profile")
    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @Operation(summary = "Current volunteer certification")
    @GetMapping("/me/certification")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<VolunteerCertification> myCertification() {
        return ApiResponse.success(userService.getCurrentUserCertification());
    }

    @Operation(summary = "Submit current volunteer certification")
    @PostMapping("/me/certification")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ApiResponse<Void> submitMyCertification(@Valid @RequestBody VolunteerCertificationSubmitRequest request) {
        userService.submitCurrentUserCertification(request);
        return ApiResponse.success("submitted", null);
    }

    @Operation(summary = "Admin - volunteer certification detail")
    @GetMapping("/{id}/certification")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VolunteerCertification> certificationDetail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserCertificationDetail(id));
    }

    @Operation(summary = "Admin - audit volunteer certification")
    @PutMapping("/{id}/certification/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> auditCertification(@PathVariable Long id,
                                                @Valid @RequestBody VolunteerCertificationAuditRequest request) {
        userService.auditUserCertification(id, request);
        return ApiResponse.success("audited", null);
    }

    @Operation(summary = "Update current profile")
    @PutMapping("/me")
    public ApiResponse<Void> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateCurrentUser(request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Update password")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ApiResponse.success("updated", null);
    }
}
