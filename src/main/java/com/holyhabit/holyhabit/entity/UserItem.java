package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 유저가 가진 아이템 (계정 단위)
// - EFFECT: quantity 항상 1, equipped = 착용 중
// - 나중에 MATERIAL: quantity가 쌓임, equipped 안 씀
@Entity
@Table(name = "user_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean equipped = false;

    // 얻은 경로: DEFAULT(기본 이펙트 착용 시 생성) / PURCHASE(인앱결제) / ADMIN(직접 지급) / DROP(몬스터) 등
    @Column(length = 20)
    private String source;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt;

    @PrePersist
    protected void onCreate() {
        if (this.acquiredAt == null) this.acquiredAt = LocalDateTime.now();
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }
}
