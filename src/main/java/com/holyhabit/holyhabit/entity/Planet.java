package com.holyhabit.holyhabit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "planets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;           // 예: 소방관 행성

    @Column(columnDefinition = "TEXT")
    private String description;    // 행성 설명

    private String imageKey;       // 행성 이미지

    private String theme;          // 새벽 / 산악 / 도심 등

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true; // 공개 여부

    // 인터뷰 인물 정보
    private String interviewPersonName;    // 소방관 김민수
    private String interviewPersonJob;     // 소방관
    private String interviewMessage;       // 행성 완성 시 표시될 메시지

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}