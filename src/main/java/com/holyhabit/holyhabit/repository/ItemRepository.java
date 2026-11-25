package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(String type);
}
