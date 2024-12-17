package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.repository.queryDSL.ReportRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    // 토론에 대한 신고 수
    long countByDiscussionAndStatus(Discussion discussion, ReportStatus status);

    // 댓글에 대한 신고 수
    long countByCommentAndStatus(DiscussionComment discussionComment, ReportStatus status);

    // 토론에 대한 신고 리스트
    List<Report> findByDiscussionAndStatus(Discussion discussion, ReportStatus status);

    // 댓글에 대한 신고 리스트
    List<Report> findByCommentAndStatus(DiscussionComment discussionComment, ReportStatus status);

    // 회원이 그 댓글을 신고 했는지 확인
    Optional<Report> findByReporterAndComment(Member reporter, DiscussionComment comment);
    
    @Query("SELECT r from Report r "
            + "left join fetch r.reporter "
            + "left join fetch r.reported "
            + "left join fetch r.discussion "
            + "left join fetch r.comment "
            + "where r.id = :reportId")
    Optional<Report> findByIdWithAll(@Param("reportId") Long reportId);
    
    Boolean existsByReporterIdAndDiscussionId(Long loginMemberId, Long discussionId);


}
