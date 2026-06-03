package com.holyhabit.holyhabit.scheduler;

import com.holyhabit.holyhabit.repository.BattleLogRepository;
import com.holyhabit.holyhabit.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleCleanupScheduler {

    private final BattleRepository battleRepository;
    private final BattleLogRepository battleLogRepository;

    // 매일 새벽 3시 실행
    // 미수령 배틀 7일 지난 것 삭제 (신발 코인은 이미 소모됨)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredPendingBattles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        // battle_logs 먼저 삭제 (FK 제약 때문에)
        int logsDeleted = battleLogRepository.deleteLogsOfExpiredPendingBattles(cutoff);
        int battlesDeleted = battleRepository.deleteExpiredPendingBattles(cutoff);

        log.info("[스케줄러] 미수령 배틀 정리 완료 — battles: {}건, battle_logs: {}건 삭제",
                battlesDeleted, logsDeleted);
    }

    // 매일 새벽 3시 10분 실행
    // 보상 수령 완료된 battles 30일 지난 것 삭제 (battle_logs는 보상 수령 시 이미 삭제됨)
    @Scheduled(cron = "0 10 3 * * *")
    @Transactional
    public void deleteOldCompletedBattles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        int deleted = battleRepository.deleteOldCompletedBattles(cutoff);

        log.info("[스케줄러] 완료 배틀 정리 완료 — battles: {}건 삭제", deleted);
    }
}
