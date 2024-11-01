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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE discussion_comment SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DiscussionComment extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;  // 어떤 토론의 댓글인지
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 작성자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DiscussionComment parent;  // 부모 댓글, 없으면 null
    
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    @Builder.Default
    private List<DiscussionComment> children = new ArrayList<>();  // 자식 댓글들
    
    // === 내용 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;  // 찬성/반대 의견
    
    @Column(length = 2000, nullable = false)
    private String content;  // 댓글 내용
    
    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;  // 좋아요 수
    
    @ElementCollection
    @CollectionTable(
            name = "discussion_comment_likes",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Builder.Default
    private Set<Long> likedByMembers = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isSelected = false;  // 채택된 답변인지, 댓글의 최상위로 좋아요 많이 받은 3개를 올리기 위한?
    
    @Column(nullable = false)
    @Builder.Default
    private Integer reportCount = 0;  // 신고 수
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}