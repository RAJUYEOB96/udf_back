package com.undefinedus.backend.scheduler.repository;

import com.undefinedus.backend.scheduler.entity.QuartzTrigger;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuartzTriggerRepository extends JpaRepository<QuartzTrigger, Long> {

    @Modifying
    @Query("DELETE FROM QuartzTrigger q WHERE q.triggerName = :triggerName")
    void deleteByTriggerName(@Param("triggerName") String triggerName);

    Optional<QuartzTrigger> findByTriggerName(String triggerName);
}
