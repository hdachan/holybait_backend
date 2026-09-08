package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.*;
import com.holyhabit.holyhabit.entity.User;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse response = authService.loginWithGoogle(
                request.getIdToken(),
                request.getDeviceInfo(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(response);
    }

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
        return ResponseEntity.ok(
                new LoginResponse(tokens.accessToken(), tokens.refreshToken(), false));
    }

    // 동의 완료 처리
    @PostMapping("/consent")
    public ResponseEntity<Void> consent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ConsentRequest request
    ) {
        authService.completeConsent(
                userDetails.getUserId(),
                request.isMarketingAgreed());
        return ResponseEntity.ok().build();
    }

    // 마케팅 동의 업데이트
    @PatchMapping("/consent/marketing")
    public ResponseEntity<Void> updateMarketing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Boolean> body
    ) {
        authService.updateMarketingConsent(
                userDetails.getUserId(),
                Boolean.TRUE.equals(body.get("marketingAgreed")));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenRefreshRequest request) {
        tokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        tokenService.revokeAllTokens(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.withdraw(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(toResponseMap(userDetails.getUser()));
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateNicknameRequest request
    ) {
        User updated = authService.updateNickname(
                userDetails.getUserId(), request.getNickname());
        return ResponseEntity.ok(toResponseMap(updated));
    }

    private Map<String, Object> toResponseMap(User user) {
        return Map.of(
                "uuid",             user.getUuid(),
                "email",            user.getEmail(),
                "nickname",         user.getNickname(),
                "provider",         user.getProvider(),
                "status",           user.getStatus(),
                "createdAt",        user.getCreatedAt().toString(),
                "marketingAgreed",  user.isMarketingAgreed(),
                "consentCompleted", user.isConsentCompleted()
        );
    }
}