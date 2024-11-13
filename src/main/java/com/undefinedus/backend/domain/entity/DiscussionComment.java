package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.VoteType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE discussion_comment SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@ToString(exclude = {"discussion", "member", "parent", "children"})  // 순환참조 방지
public class DiscussionComment extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;  // 어떤 토론의 댓글인지
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 작성자
    
    // 대댓글 단순한 버전 =================================
    @Column
    private Long parentId; // 부모Id // 고유 아이디는 id로 사용
    
    @Column
    private Long groupId;
    
    @Column(name = "comment_order")  // 'order' 대신 'comment_order' 사용
    private Long order;
    
    // === 내용 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;  // 찬성/반대 의견
    
    @Column(length = 2000, nullable = false)
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
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}