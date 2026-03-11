package com.community.modules.user.controller;

import com.community.common.web.ApiResponse;
import com.community.common.web.PageResult;
import com.community.modules.user.dto.PasswordUpdateRequest;
import com.community.modules.user.dto.UserUpdateRequest;
import com.community.modules.user.entity.User;
import com.community.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                                              @RequestParam(required = false) String keyword) {
        return ApiResponse.success(userService.pageUsers(current, size, role, keyword));
    }

    @Operation(summary = "Admin - update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateByAdmin(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.adminUpdateUser(id, request);
        return ApiResponse.success("updated", null);
    }

    @Operation(summary = "Current user profile")
    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.success(userService.getCurrentUser());
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

