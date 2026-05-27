// StageRepository.java
package com.holyhabit.holyhabit.repository;

import com.holyhabit.holyhabit.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StageRepository extends JpaRepository<Stage, Long> {
    List<Stage> findAllByOrderByIdAsc();
}
