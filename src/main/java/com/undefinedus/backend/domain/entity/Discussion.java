package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.DiscussionStatus;
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
@SQLDelete(sql = "UPDATE discussion SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Discussion extends BaseEntity{
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;  // 어떤 책의 토론인지
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 작성자
    
    // === 토론 주제 === //
    @Column(length = 200, nullable = false)
    private String title;    // 토론 제목
    
    @Column(length = 2000, nullable = false)
    private String content;  // 토론 내용
    
    @Column(length = 50)
    private String category;  // 토론 카테고리 (예: 줄거리, 해석, 감상 등)
    
    // === 토론 상태 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DiscussionStatus status = DiscussionStatus.WAITING;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;  // 좋아요 수 (토론 주제 추천수)
    
    @ElementCollection
    @CollectionTable(
            name = "discussion_likes",  // 좋아요 정보를 저장할 테이블 이름
            joinColumns = @JoinColumn(name = "discussion_id")
    )
    @Builder.Default
    private Set<Long> likedByMembers = new HashSet<>();  // 좋아요 누른 회원 ID 목록
    
    @Column
    private LocalDateTime closedAt;    // status가 CLOSED일 경우 기록, 토론 종료 시간
    
    @OneToMany(mappedBy = "discussion")
    private List<DiscussionComment> comments = new ArrayList<>();  // 토론에 달린 댓글들
    
    // === 토론 결과 === //
    @Column(nullable = false)
    @Builder.Default
    private Integer participantCount = 0;  // 참여자 수, 댓글을 작성한 유저 수 (중복 X)
    
    @Column(length = 2000)
    private String conclusion;  // AI가 분석한 토론 결론/요약
    
    @Column
    private Boolean result;    // AI가 분석한 최종 결과 (true: 찬성 우세, false: 반대 우세, null: 판단불가)
    
    @Column(length = 1000)
    private String reasoning;  // AI의 결과 도출 근거
    
    // === 신고 관련 === //
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
