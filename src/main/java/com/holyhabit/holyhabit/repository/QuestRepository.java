package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findAllByTypeAndIsActiveTrueOrderBySortOrder(String type);

    List<Quest> findAllByActionTypeAndIsActiveTrue(String actionType);
}