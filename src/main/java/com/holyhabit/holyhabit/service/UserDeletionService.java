package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.entity.User;
import com.holyhabit.holyhabit.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원 탈퇴 — 이용약관 제10조 "탈퇴 시 계정 정보 및 모든 게임 데이터 삭제"
// - 운동·루틴·커스텀 운동·걸음·캐릭터·배틀·재화·퀘스트·토큰 즉시 삭제
// - users 행은 번호만 남기고 개인정보(이메일·닉네임·구글 ID)를 지움
//   → login_history(접속 로그, 3개월 보관)가 이 행을 참조하기 때문
//   → 구글 ID가 지워지므로 같은 구글 계정으로 다시 로그인하면 새 계정으로 가입됨
// FK 순서대로 자식 테이블부터 지움
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeletionService {

    private final EntityManager em;
    private final UserRepository userRepository;

    private static final String MY_CUSTOM_EXERCISES =
            "SELECT e.id FROM Exercise e WHERE e.user.id = :uid";
    private static final String MY_ROUTINE_EXERCISES =
            "SELECT re.id FROM RoutineExercise re WHERE re.routine.user.id = :uid";

    @Transactional
    public void deleteUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 운동 기록 — 내 기록 + (혹시 있다면) 내 커스텀 운동을 쓴 기록
        String logsToDelete = "SELECT wl.id FROM WorkoutLog wl WHERE wl.user.id = :uid"
                + " OR wl.exercise.id IN (" + MY_CUSTOM_EXERCISES + ")";
        int sets = run("DELETE FROM WorkoutSet ws WHERE ws.workoutLog.id IN (" + logsToDelete + ")", userId);
        int logs = run("DELETE FROM WorkoutLog wl WHERE wl.user.id = :uid"
                + " OR wl.exercise.id IN (" + MY_CUSTOM_EXERCISES + ")", userId);
        // 다른 사람 기록이 내 루틴 항목을 참고로 가리키고 있으면 연결만 끊음
        run("UPDATE WorkoutLog wl SET wl.routineExercise = null"
                + " WHERE wl.routineExercise.id IN (" + MY_ROUTINE_EXERCISES + ")", userId);

        // 루틴 · 커스텀 운동
        // (MySQL은 지우는 테이블 자신을 서브쿼리로 조회하면 에러 1093 → routines 기준으로 찾음)
        run("DELETE FROM RoutineExercise re"
                + " WHERE re.routine.id IN (SELECT r.id FROM Routine r WHERE r.user.id = :uid)"
                + " OR re.exercise.id IN (" + MY_CUSTOM_EXERCISES + ")", userId);
        int routines = run("DELETE FROM Routine r WHERE r.user.id = :uid", userId);
        run("DELETE FROM Exercise e WHERE e.user.id = :uid", userId);

        // 걸음 · 캐릭터 · 배틀 · 재화 · 퀘스트 · 토큰
        int steps = run("DELETE FROM StepLog s WHERE s.user.id = :uid", userId);
        run("DELETE FROM BattleLog bl WHERE bl.battle.id IN (SELECT b.id FROM Battle b WHERE b.user.id = :uid)", userId);
        int battles = run("DELETE FROM Battle b WHERE b.user.id = :uid", userId);
        int characters = run("DELETE FROM CharacterStat cs WHERE cs.user.id = :uid", userId);
        run("DELETE FROM CurrencyLog cl WHERE cl.user.id = :uid", userId);
        run("DELETE FROM UserCurrency uc WHERE uc.user.id = :uid", userId);
        run("DELETE FROM UserQuest uq WHERE uq.user.id = :uid", userId);
        run("DELETE FROM RefreshToken rt WHERE rt.user.id = :uid", userId);

        // 개인정보 지우기 (users 행은 접속 로그 보관용으로 번호만 남김)
        user.anonymize();

        log.info("[탈퇴] userId={} 삭제 — 운동기록 {}건(세트 {}), 루틴 {}, 걸음 {}, 배틀 {}, 캐릭터 {}",
                userId, logs, sets, routines, steps, battles, characters);
    }

    private int run(String jpql, Long userId) {
        return em.createQuery(jpql).setParameter("uid", userId).executeUpdate();
    }
}
