package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Getter
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reporter_id", "discussion_id"}),
    @UniqueConstraint(columnNames = {"reporter_id", "discussion_comment_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 신고자 === //
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    // DB 레벨에서는 해당 member가 없으면 null로 처리됩니다.
    // 하지만 JPA는 report.reporter_id나 report.reported_id 값이 존재하면(즉, FK가 있으면) 해당 Member 엔티티를 로드하려고 시도합니다.
    // 이때 Member 테이블에서 해당 ID를 가진 레코드가 없으면 EntityNotFoundException이 발생합니다.
    // 그래서 Report 엔티티에서 Member 관계 설정을 수정
    @NotFound(action = NotFoundAction.IGNORE)  // 추가
    @JoinColumn(name = "reporter_id", nullable = false) // 신고를 한 사람의 ID
    private Member reporter;  // 신고한 사람
    
    // === 신고 대상 === //
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @NotFound(action = NotFoundAction.IGNORE)  // 추가
    @JoinColumn(name = "reportee_id", nullable = false) // 신고를 당한 사람의 ID
    private Member reported;  // 신고 당한 사람
    
    // === 신고 대상 정보 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;  // 신고 대상 타입 (Discussion, Comment 등)

    @Column(nullable = false)
    private String reportReason;
    
    @Column(nullable = false, length = 20)  // 'TEMPORARY_ACCEPTED' 길이를 고려한 충분한 길이
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING; // 처리 상태 (PENDING, TEMPORARY_ACCEPTED, ACCEPTED, REJECTED)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", updatable = false)
    private Discussion discussion;  // 신고 당한 토론 게시물 // comment가 존재하면 null
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_comment_id", updatable = false)
    private DiscussionComment comment;  // 신고 당한 토론 댓글 게시물 // discussion가 존재하면 null
    
    @Column(length = 20)    // enum이 길이를 위해
    @Enumerated(EnumType.STRING)
    private DiscussionStatus previousDiscussionStatus;  // discussion에만 사용할 예정

    public void changeStatus(ReportStatus status) {
        this.status = status;
    }
    
    public void changePreviousDiscussionStatus(DiscussionStatus previousDiscussionStatus) {
        this.previousDiscussionStatus = previousDiscussionStatus;
    }
}
