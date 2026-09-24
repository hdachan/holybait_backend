package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_quests",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "quest_id", "period_key"}))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    // tutorial: null (1회성, 주기 없음)
    // daily:    "2026-09-19" (날짜)
    // weekly:   "2026-W38"   (주차)
    @Column(name = "period_key", length = 20)
    private String periodKey;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "in_progress"; // in_progress / completed / claimed

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── 진행도 증가 (일반 퀘스트 - 행동 발생마다 +amount) ──
    public void increaseProgress(int amount) {
        if ("claimed".equals(this.status)) return;
        this.progress += amount;
        if (this.progress >= this.quest.getTargetCount()
                && !"completed".equals(this.status)
                && !"claimed".equals(this.status)) {
            this.status = "completed";
            this.completedAt = LocalDateTime.now();
        }
    }

    // ── 진행도 직접 설정 (achievement - 누적값 그대로 반영, ex: total_steps) ──
    public void setProgressValue(int value) {
        if ("claimed".equals(this.status)) return;
        this.progress = value;
        if (this.progress >= this.quest.getTargetCount()
                && !"completed".equals(this.status)
                && !"claimed".equals(this.status)) {
            this.status = "completed";
            this.completedAt = LocalDateTime.now();
        }
    }

    // ── 보상 수령 ──
    public void claim() {
        this.status = "claimed";
        this.claimedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return "completed".equals(this.status);
    }

    public boolean isClaimed() {
        return "claimed".equals(this.status);
    }
}