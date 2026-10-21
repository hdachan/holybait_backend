package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.Quest;
import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.entity.UserQuest;
import com.holyhabit.holyhabit.repository.QuestRepository;
import com.holyhabit.holyhabit.repository.UserQuestRepository;
import com.holyhabit.holyhabit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 일일·주간 퀘스트 진행도 — 오늘(이번 주) 칸이 아직 없으면 만들고 올림
// 퀘스트 창을 열지 않은 채 자정(월요일 0시)을 넘겨도 진행이 잡히도록
//
// 별도 트랜잭션(REQUIRES_NEW)인 이유
// - 운동 저장·배틀 보상 같은 바깥 작업과 분리: 여기서 실패해도 바깥 작업은 정상 저장됨
// - 바깥 트랜잭션은 이미 조회를 해서 그 시점 데이터만 보이므로(MySQL REPEATABLE READ),
//   새로 만든 칸을 바깥에서는 찾을 수 없음 → 만들기와 진행도 올리기를 이 안에서 함께 처리
// 동시에 같은 칸을 만들면 (user, quest, period_key) 유니크 제약이 하나만 남기고 막음
// → 이 트랜잭션은 실패, 호출하는 쪽(QuestService)이 한 번 다시 시도
@Slf4j
@Service
@RequiredArgsConstructor
public class PeriodQuestService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void progress(Long userId, String type, String periodKey, String actionType, int amount) {
        User user = userRepository.getReferenceById(userId);

        for (Quest quest : questRepository.findAllByTypeAndIsActiveTrueOrderBySortOrder(type)) {
            boolean exists = userQuestRepository
                    .findByUserIdAndQuestIdAndPeriodKey(userId, quest.getId(), periodKey)
                    .isPresent();
            if (!exists) {
                userQuestRepository.save(UserQuest.builder()
                        .user(user)
                        .quest(quest)
                        .periodKey(periodKey)
                        .progress(0)
                        .status("in_progress")
                        .build());
            }
        }
        userQuestRepository.flush();

        List<UserQuest> targets = userQuestRepository
                .findInProgressByUserIdAndActionType(userId, actionType, periodKey);
        for (UserQuest uq : targets) {
            uq.increaseProgress(amount);
            if (uq.isCompleted()) {
                log.info("userId={} 퀘스트 완료: {}", userId, uq.getQuest().getTitle());
            }
        }
    }
}
