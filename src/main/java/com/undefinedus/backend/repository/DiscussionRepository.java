package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.repository.queryDSL.DiscussionRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscussionRepository extends JpaRepository<Discussion, Long>,
    DiscussionRepositoryCustom {

    @Query("SELECT d FROM Discussion d WHERE d.status NOT IN ('COMPLETED', 'BLOCKED')")
    List<Discussion> findAllByStatusNotClosed();

    @Query("SELECT d FROM Discussion d LEFT JOIN FETCH d.participants WHERE d.id = :discussionId")
    Optional<Discussion> findByIdWithParticipants(@Param("discussionId") Long discussionId);
    
    // 삭제 처리된 discussion까지 찾기 위해
    @Query("SELECT d FROM Discussion d WHERE d.member.id = :memberId AND d.myBook.id = :bookId")
    List<Discussion> findByMemberIdAndBookId(Long memberId, Long bookId);
}
