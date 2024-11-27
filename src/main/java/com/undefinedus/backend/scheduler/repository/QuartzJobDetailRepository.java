package com.undefinedus.backend.scheduler.repository;

import com.undefinedus.backend.scheduler.entity.QuartzJobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuartzJobDetailRepository extends JpaRepository<QuartzJobDetail, Long> {

//    void deleteByJobName(String jobName);

    @Modifying
    @Query("DELETE FROM QuartzJobDetail q WHERE q.jobName = :jobName")
    void deleteByJobName(@Param("jobName") String jobName);
}
