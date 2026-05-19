package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.LoginRequest;
import com.holyhabit.holyhabit.controller.dto.LoginResponse;
import com.holyhabit.holyhabit.controller.dto.TokenRefreshRequest;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.AuthService;
import com.holyhabit.holyhabit.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        TokenService.TokenPair tokens = authService.loginWithGoogle(
                request.getIdToken(),
                request.getDeviceInfo(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        TokenService.TokenPair tokens = tokenService.refresh(
                request.getRefreshToken(),
                request.getDeviceInfo(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(new LoginResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    // 단일 기기 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRefreshRequest request) {
        tokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    // 전체 기기 로그아웃
    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        tokenService.revokeAllTokens(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.withdraw(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    // 내 정보 조회 — createdAt 추가
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var user = userDetails.getUser();
        return ResponseEntity.ok(Map.of(
                "uuid",      user.getUuid(),
                "email",     user.getEmail(),
                "nickname",  user.getNickname(),
                "provider",  user.getProvider(),
                "status",    user.getStatus(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }
}
