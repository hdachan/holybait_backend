package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.LoginResponse;
import com.holyhabit.holyhabit.entity.*;
import com.holyhabit.holyhabit.repository.LoginHistoryRepository;
import com.holyhabit.holyhabit.repository.UserRepository;
import com.holyhabit.holyhabit.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OAuthService oAuthService;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse loginWithGoogle(String idToken, String deviceInfo, String ipAddress) {

        OAuthService.GoogleUserInfo googleUser = oAuthService.verifyGoogleToken(idToken);

        User user = userRepository
                .findByProviderAndProviderId(Provider.GOOGLE, googleUser.providerId())
                .orElseGet(() -> registerUser(googleUser));

        if (user.getStatus() == UserStatus.BANNED)  throw new RuntimeException("403_001");
        if (user.getStatus() == UserStatus.DELETED) throw new RuntimeException("403_002");

        user.updateLastLogin();

        String accessToken  = jwtProvider.generateAccessToken(user.getId(), "USER");
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        tokenService.saveRefreshToken(user, refreshToken, deviceInfo, ipAddress);
        saveLoginHistory(user, ipAddress, deviceInfo, LoginHistory.LoginStatus.SUCCESS);

        // consentCompleted = false면 동의 화면 표시
        return new LoginResponse(
                accessToken, refreshToken, !user.isConsentCompleted());
    }

    // 동의 완료 처리
    @Transactional
    public void completeConsent(Long userId, boolean marketingAgreed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.completeConsent(marketingAgreed);
        log.info("userId={} 동의 완료 marketing={}", userId, marketingAgreed);
    }

    // 마케팅 동의 업데이트
    @Transactional
    public void updateMarketingConsent(Long userId, boolean marketingAgreed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.updateMarketingAgreed(marketingAgreed);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.softDelete();
        tokenService.revokeAllTokens(userId);
    }

    @Transactional
    public User updateNickname(Long userId, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        String trimmed = nickname.trim();
        if (trimmed.length() < 2 || trimmed.length() > 12) {
            throw new IllegalArgumentException("닉네임은 2~12자로 입력해주세요.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.updateNickname(trimmed);
        return user;
    }

    private User registerUser(OAuthService.GoogleUserInfo googleUser) {
        return userRepository.save(User.builder()
                .uuid(UUID.randomUUID().toString())
                .email(googleUser.email())
                .nickname(googleUser.name() != null ? googleUser.name() : "모험가")
                .provider(Provider.GOOGLE)
                .providerId(googleUser.providerId())
                .status(UserStatus.ACTIVE)
                .build());
    }

    private void saveLoginHistory(User user, String ip, String deviceInfo,
                                  LoginHistory.LoginStatus status) {
        loginHistoryRepository.save(LoginHistory.builder()
                .user(user).ipAddress(ip).deviceInfo(deviceInfo).status(status)
                .build());
    }
}