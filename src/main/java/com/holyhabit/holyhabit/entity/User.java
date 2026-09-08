package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // 캐릭터 슬롯 수 (기본 4, 최대 10)
    @Column(nullable = false)
    @Builder.Default
    private int slotCount = 4;

    // 동의 완료 여부 (false면 동의 화면 표시)
    @Column(nullable = false)
    @Builder.Default
    private boolean consentCompleted = false;

    // 마케팅 수신 동의 (선택)
    @Column(nullable = false)
    @Builder.Default
    private boolean marketingAgreed = false;

    private LocalDateTime lastLoginAt;
    private LocalDateTime deletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = UserStatus.ACTIVE;
        if (this.slotCount == 0) this.slotCount = 4;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() { this.lastLoginAt = LocalDateTime.now(); }

    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateNickname(String nickname) { this.nickname = nickname; }

    public boolean expandSlot() {
        if (this.slotCount >= 10) return false;
        this.slotCount++;
        return true;
    }

    // 동의 완료 처리
    public void completeConsent(boolean marketingAgreed) {
        this.consentCompleted = true;
        this.marketingAgreed = marketingAgreed;
    }

    // 마케팅 동의 업데이트
    public void updateMarketingAgreed(boolean marketingAgreed) {
        this.marketingAgreed = marketingAgreed;
    }
}