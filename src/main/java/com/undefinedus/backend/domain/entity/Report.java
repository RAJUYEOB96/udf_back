package com.undefinedus.backend.domain.entity;

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
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE report SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Report {
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 신고자 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 신고한 사람
    
    // === 신고 대상 정보 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;  // 신고 대상 타입 (Discussion, Comment 등)
    
    // === 신고 정보 === //
    @Column(length = 500, nullable = false)
    private String reason;  // 신고 사유
    
    // 신고당한 게시물 안에 작성자의 정보도 들어있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", updatable = false)
    private Discussion discussion;  // 신고 당한 토론 게시물 // comment가 존재하면 null
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_comment_id", updatable = false)
    private DiscussionComment comment;  // 신고 당한 토론 댓글 게시물 // discussion가 존재하면 null
    
    // === 처리 상태 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isProcessed = false;  // 신고 처리 여부
    
    @Column
    private LocalDateTime processedAt;  // 신고 처리 시간
    
    @Column(length = 500)
    private String processResult;  // 신고 처리 결과
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // === 연관 관계 설정 메서드 === //
    public void setTarget(Discussion discussion) {
        if (discussion == null) {
            throw new IllegalArgumentException("해당 토론이 존재하지 않습니다.");
        }
        this.targetType = ReportTargetType.DISCUSSION;
        this.discussion = discussion;
        this.comment = null;
    }
    
    public void setTarget(DiscussionComment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("해당 댓글이 존재하지 않습니다.");
        }
        this.targetType = ReportTargetType.DISCUSSION_COMMENT;
        this.comment = comment;
        this.discussion = null;
    }
}
