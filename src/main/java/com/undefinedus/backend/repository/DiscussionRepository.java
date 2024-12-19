package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.repository.queryDSL.DiscussionRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscussionRepository extends JpaRepository<Discussion, Long>,
    DiscussionRepositoryCustom {

    @Query("SELECT d FROM Discussion d WHERE d.status NOT IN ('COMPLETED', 'BLOCKED')")
    List<Discussion> findAllByStatusNotClosed();

    @Query("SELECT d FROM Discussion d LEFT JOIN FETCH d.participants WHERE d.id = :discussionId")
    Optional<Discussion> findByIdWithParticipants(@Param("discussionId") Long discussionId);
    
    @Modifying
    @Query("UPDATE Discussion d SET d.views = d.views + 1 WHERE d.id = :discussionId")
    void increaseViews(@Param("discussionId") Long discussionId);
    
    @Query("SELECT d.views FROM Discussion d WHERE d.id = :discussionId")
    Long findViewsById(@Param("discussionId") Long discussionId);

    List<Discussion> findAllByStatus(DiscussionStatus discussionStatus);
}
