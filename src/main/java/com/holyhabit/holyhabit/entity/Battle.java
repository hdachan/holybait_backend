package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_id", nullable = false)
    private Monster monster;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleResult result;

    // false = 아직 보상 미수령, true = 마지막 확인 버튼 눌러서 보상 수령 완료
    @Column(nullable = false)
    @Builder.Default
    private boolean rewardsClaimed = false;

    // 배틀한 캐릭터와 그때 스탯 — 이어하기 화면과 보상 지급에 사용
    // (FK 없이 id만 저장: 캐릭터를 삭제해도 배틀 기록이 막히지 않도록. 이전 배틀은 null)
    private Long characterStatId;
    private Integer playerMaxHp;
    private Integer playerAtk;
    private Integer playerDef;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }

    public void claimRewards() { this.rewardsClaimed = true; }
}
