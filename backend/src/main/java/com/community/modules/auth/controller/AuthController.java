package com.community.modules.auth.controller;

import com.community.common.web.ApiResponse;
import com.community.modules.auth.dto.AuthTokenResponse;
import com.community.modules.auth.dto.LoginRequest;
import com.community.modules.auth.dto.RegisterRequest;
import com.community.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login (admin or volunteer)")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "Volunteer register")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("registered", null);
    }
}

