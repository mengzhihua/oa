package com.oa.system.controller;

import com.oa.common.R;
import com.oa.system.auth.CurrentUser;
import com.oa.system.dto.LoginRequest;
import com.oa.system.dto.PasswordRequest;
import com.oa.system.service.AuthService;
import com.oa.system.vo.LoginResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/me")
    public R<LoginResponse> me() {
        return R.ok(authService.current(CurrentUser.id()));
    }

    @PutMapping("/password")
    public R<Void> password(@Valid @RequestBody PasswordRequest request) {
        authService.changePassword(CurrentUser.id(), request.getOldPassword(),
                request.getNewPassword());
        return R.ok();
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
