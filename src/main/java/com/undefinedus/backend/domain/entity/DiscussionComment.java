package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.DiscussionCommentStatus;
import com.undefinedus.backend.domain.enums.VoteType;
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
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE discussion_comment SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@ToString(exclude = {"discussion", "member"})  // 순환참조 방지
public class DiscussionComment extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // groupId와 같다고 보면 됨
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;  // 어떤 토론의 댓글인지
    
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)  // 추가
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 작성자
    
    // 대댓글 단순한 버전 =================================

    @Column
    private Long groupId;

    @Column
    private Long parentId; // 부모Id // 고유 아이디는 id로 사용
    
    @Column
    private Long groupOrder;

    @Column
    @Builder.Default
    private boolean isChild = false; // id를 조회하고 나온 것을 가지고 isChild count 해서 조회하면 자식의 ord 를 쉽게 알 수 있다.

    @Column
    private Long totalOrder; // 자식 포함 전체 댓글의 실제 보여지는 순서

    // === 내용 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;  // 찬성/반대 의견
    
    @Column(length = 300, nullable = false)
    private String content;  // 댓글 내용
    
    // === 좋아요/싫어요 === //
    // like, dislike count는 CommentLike 엔티티에서 계산하는 것이 좋을 것 같습니다
    @OneToMany(mappedBy = "comment", orphanRemoval = true)
    @Builder.Default
    private List<CommentLike> likes = new ArrayList<>();  // likeCount, likedByMembers 대체
    
    // === 토론 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isSelected = false;  // 채택된 답변인지, 댓글의 최상위로 좋아요 많이 받은 3개를 올리기 위한? 잘 모르겠음

    // === 신고 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private DiscussionCommentStatus discussionCommentStatus = DiscussionCommentStatus.ACTIVE; // 누적 신고 3회 이상시 자동으로 BLOCKED 처리 됨

    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void changeId(Long id) {
        this.id = id;
    }

    public void changeDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public void changeGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void changeDiscussionCommentStatus(
        DiscussionCommentStatus discussionCommentStatus) {
        this.discussionCommentStatus = discussionCommentStatus;
    }
}