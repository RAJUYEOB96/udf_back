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
import jakarta.persistence.Lob;
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
@SQLDelete(sql = "UPDATE discussion SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@ToString(exclude = {"myBook", "member", "participants", "comments"})  // 실제 연관관계 있는 필드만 exclude
public class Discussion extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private MyBook myBook;  // 어떤 책의 토론인지 // 내가 기록한 책만 토론 주제로 올릴 수 있음
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 작성자
    
    // === 토론 주제 === //
    @Column(length = 200, nullable = false)
    private String title;    // 토론 제목
    
    @Lob  // 긴 내용을 위해 BLOB으로 변경
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;
    
    // === 토론 상태 === //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DiscussionStatus status = DiscussionStatus.PROPOSED;
    
    @OneToMany(mappedBy = "discussion", orphanRemoval = true)
    @Builder.Default
    private List<DiscussionParticipant> participants = new ArrayList<>(); // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(찬성 2/ 반대 2)
    
    @Column
    private LocalDateTime startDate; // 토론을 시작할 시간 // 토론 시작 시간은 createdDate보다 최소 24시간 뒤 최대 7일 이여야 한다.
    
    @Column
    private LocalDateTime closedAt;    // status가 CLOSED일 경우 기록, 토론 종료 시간(시작 시간으로 부터 24시간 후)
    
    @OneToMany(mappedBy = "discussion", orphanRemoval = true)
    @Builder.Default
    private List<DiscussionComment> comments = new ArrayList<>();  // 토론에 달린 댓글들
    
    // === 토론 결과 === //
    @Column(length = 2000)
    private String conclusion;  // AI가 분석한 토론 결론/요약
    
    @Column
    private Boolean result;    // AI가 분석한 최종 결과 (true: 찬성 우세, false: 반대 우세, null: 판단불가)
    
    @Column
    private Double agreePercent; // AI가 분석한 결과 퍼센트 .1 자리수 까지 (여긴 찬성 쪽)
    
    @Column
    private Double disagreePercent; // 여긴 반대 우리가 계산해서 넣기 (100 - agreePercent)
    
    @Column(length = 1000)
    private String reasoning;  // AI의 결과 도출 근거
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
