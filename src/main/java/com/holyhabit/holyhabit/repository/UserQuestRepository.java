package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.UserQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    List<UserQuest> findAllByUserId(Long userId);

    // tutorial (periodKey = null) 용
    @Query("SELECT uq FROM UserQuest uq " +
            "WHERE uq.user.id = :userId AND uq.quest.id = :questId " +
            "AND uq.periodKey IS NULL")
    Optional<UserQuest> findByUserIdAndQuestIdNoPeriod(
            @Param("userId") Long userId,
            @Param("questId") Long questId
    );

    // daily/weekly (periodKey 지정) 용
    Optional<UserQuest> findByUserIdAndQuestIdAndPeriodKey(
            Long userId, Long questId, String periodKey
    );

    // periodKey 상관없이 조회 (보상 수령 시 사용)
    Optional<UserQuest> findFirstByUserIdAndQuestIdOrderByIdDesc(
            Long userId, Long questId
    );

    // tutorial 목록 조회 (periodKey 없음)
    @Query("SELECT uq FROM UserQuest uq " +
            "JOIN FETCH uq.quest q " +
            "WHERE uq.user.id = :userId AND q.type = 'tutorial' " +
            "ORDER BY q.sortOrder")
    List<UserQuest> findTutorialByUserId(@Param("userId") Long userId);

    // periodKey 없는 타입 전체 조회 (tutorial, achievement 등 범용)
    @Query("SELECT uq FROM UserQuest uq " +
            "JOIN FETCH uq.quest q " +
            "WHERE uq.user.id = :userId AND q.type = :type " +
            "AND uq.periodKey IS NULL " +
            "ORDER BY q.sortOrder")
    List<UserQuest> findByUserIdAndTypeNoPeriod(
            @Param("userId") Long userId,
            @Param("type") String type
    );

    // daily/weekly 목록 조회 (현재 periodKey 기준)
    @Query("SELECT uq FROM UserQuest uq " +
            "JOIN FETCH uq.quest q " +
            "WHERE uq.user.id = :userId AND q.type = :type " +
            "AND uq.periodKey = :periodKey " +
            "ORDER BY q.sortOrder")
    List<UserQuest> findAllByUserIdAndTypeAndPeriodKey(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("periodKey") String periodKey
    );

    // 특정 actionType 진행 중인 퀘스트 — 한 번 호출에 한 종류만
    // currentPeriodKey == null → 기간 없는 퀘스트(tutorial/achievement)만
    // currentPeriodKey != null → 그 기간 퀘스트(오늘 daily 또는 이번 주 weekly)만
    // (예전 조건은 매 호출마다 기간 없는 퀘스트도 같이 찾아 한 번 행동에 2~3씩 올라갔음)
    @Query("SELECT uq FROM UserQuest uq " +
            "JOIN FETCH uq.quest q " +
            "WHERE uq.user.id = :userId AND q.actionType = :actionType " +
            "AND uq.status = 'in_progress' " +
            "AND ((:currentPeriodKey IS NULL AND uq.periodKey IS NULL) " +
            "     OR uq.periodKey = :currentPeriodKey)")
    List<UserQuest> findInProgressByUserIdAndActionType(
            @Param("userId") Long userId,
            @Param("actionType") String actionType,
            @Param("currentPeriodKey") String currentPeriodKey
    );
}