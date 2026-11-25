package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 가방에 들어가는 아이템 목록 (게임 데이터)
// type: EFFECT(내 공격 이펙트) / 나중에 MATERIAL(몬스터 재료) 등을 같은 표에 추가
@Entity
@Table(name = "items")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Item {

    public static final String TYPE_EFFECT = "EFFECT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type;

    // 앱에서 쓰는 이름 (EFFECT면 assets/images/effects/{item_key})
    @Column(name = "item_key", nullable = false, unique = true, length = 50)
    private String itemKey;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    // 인앱결제 가격(원) — 표시용. 실제 결제 금액은 Play Console 상품 가격. NULL = 판매 안 함
    @Column(name = "price_krw")
    private Integer priceKrw;

    // Play Console 상품 ID (결제 단계에서 채움)
    @Column(name = "product_id", length = 100)
    private String productId;

    // true = 모두가 처음부터 가진 것 (user_items 행 없이도 보유로 취급)
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
