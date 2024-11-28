package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.ReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 토론에 대한 신고 수
    long countByDiscussionAndStatus(Discussion discussion, ReportStatus status);

    // 댓글에 대한 신고 수
    long countByCommentAndStatus(DiscussionComment discussionComment, ReportStatus status);

    // 토론에 대한 신고 리스트
    List<Report> findByDiscussionAndStatus(Discussion discussion, ReportStatus status);

    // 댓글에 대한 신고 리스트
    List<Report> findByCommentAndStatus(DiscussionComment discussionComment, ReportStatus status);
}
