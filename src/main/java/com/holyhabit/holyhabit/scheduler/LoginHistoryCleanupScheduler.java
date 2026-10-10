package com.holyhabit.holyhabit.scheduler;

import com.holyhabit.holyhabit.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginHistoryCleanupScheduler {

    private final LoginHistoryRepository loginHistoryRepository;

    // 매일 새벽 3시 20분 실행
    // 개인정보 처리방침 "접속 로그 기록 3개월" — 탈퇴 여부와 관계없이 3개월 지난 접속 로그 삭제
    @Scheduled(cron = "0 20 3 * * *")
    @Transactional
    public void deleteOldLoginHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(3);
        int deleted = loginHistoryRepository.deleteOlderThan(cutoff);
        log.info("[스케줄러] 접속 로그 정리 완료 — login_history: {}건 삭제", deleted);
    }
}
