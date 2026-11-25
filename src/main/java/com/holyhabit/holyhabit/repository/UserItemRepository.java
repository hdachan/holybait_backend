package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    @Query("SELECT ui FROM UserItem ui JOIN FETCH ui.item i " +
            "WHERE ui.user.id = :userId AND i.type = :type")
    List<UserItem> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);
}
