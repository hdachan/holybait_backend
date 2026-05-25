package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_currencies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class UserCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private int gold = 0;

    @Column(nullable = false)
    @Builder.Default
    private int shoeCoin = 0;

    // 낙관적 락 — 동시 요청 시 중복 지급 방지
    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addGold(int amount) {
        this.gold = Math.max(0, this.gold + amount);
    }

    public void addShoeCoin(int amount) {
        this.shoeCoin = Math.max(0, this.shoeCoin + amount);
    }
}
