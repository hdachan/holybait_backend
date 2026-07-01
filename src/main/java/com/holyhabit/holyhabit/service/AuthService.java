package com.holyhabit.holyhabit.service;

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
    public TokenService.TokenPair loginWithGoogle(String idToken, String deviceInfo, String ipAddress) {

        // 1. Google idToken 검증
        OAuthService.GoogleUserInfo googleUser = oAuthService.verifyGoogleToken(idToken);

        // 2. 유저 조회 or 신규 가입
        User user = userRepository
                .findByProviderAndProviderId(Provider.GOOGLE, googleUser.providerId())
                .orElseGet(() -> registerUser(googleUser));

        // 3. 상태 확인
        if (user.getStatus() == UserStatus.BANNED)   throw new RuntimeException("403_001");
        if (user.getStatus() == UserStatus.DELETED)  throw new RuntimeException("403_002");

        // 4. 마지막 로그인 갱신
        user.updateLastLogin();

        // 5. 토큰 발급
        String accessToken  = jwtProvider.generateAccessToken(user.getId(), "USER");
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // 6. Refresh Token 저장
        tokenService.saveRefreshToken(user, refreshToken, deviceInfo, ipAddress);

        // 7. 로그인 이력 기록
        saveLoginHistory(user, ipAddress, deviceInfo, LoginHistory.LoginStatus.SUCCESS);

        return new TokenService.TokenPair(accessToken, refreshToken);
    }

    // 회원 탈퇴 (soft delete)
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        user.softDelete();
        tokenService.revokeAllTokens(userId);
    }

    // 닉네임 수정
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
                .user(user)
                .ipAddress(ip)
                .deviceInfo(deviceInfo)
                .status(status)
                .build());
    }
}