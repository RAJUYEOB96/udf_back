package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.DiscussionStatus;
import jakarta.persistence.Column;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = false")
@ToString(exclude = {"aladinBook", "member", "participants", "comments"})  // 실제 연관관계 있는 필드만 exclude
public class Discussion extends BaseEntity {

    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aladin_book_id", nullable = false)
    private AladinBook aladinBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 작성자

    // === 토론 주제 === //
    @Column(length = 100, nullable = false)
    private String title;    // 토론 제목

    @Column(length = 1000, nullable = false)
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
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startDate; // 토론을 시작할 시간 // 토론 시작 시간은 createdDate보다 최소 24시간 뒤 최대 7일 이여야 한다.

    @Column
    @Temporal(TemporalType.TIMESTAMP)
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
    private Integer agreePercent; // AI가 분석한 결과 퍼센트를 실수 형태가 아닌 정수형태로(여긴 찬성 쪽) 받을때 그렇게 받기

    @Column
    private Integer disagreePercent; // 여긴 반대 우리가 계산해서 넣기 (100 - agreePercent)

    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String reasoning;  // AI의 결과 도출 근거

    // === 조회수 관련 === //
    // @Version은:
    // 엔티티가 수정될 때마다 자동으로 버전 번호를 증가시킴
    // 다른 누군가가 먼저 수정했다면, 내가 가진 버전과 DB의 버전이 달라서 에러가 발생
    // 이를 통해 동시에 여러 사용자가 같은 데이터를 수정하는 것을 안전하게 처리할 수 있음
    @Version
    private Long version;    // 버전 관리 필드 추가 // 동시

    @Column(nullable = false)
    @Builder.Default
    private Long views = 0L;    // 조회수 기본값 0으로 초기화

    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 조회수 증가 메서드
    public void increaseViews() {
        this.views++;
    }

    // 여러 명이 동시에 조회할 경우를 대비한 메서드
    public void increaseViewsBy(Long count) {
        if (count != null && count > 0) {
            this.views += count;
        }
    }

    // 소프트 딜리트가 자동으로 안되서 수종으로 하기위한 setter
    public void changeDeleted(boolean b) {
        isDeleted = b;
    }

    public void changeDeletedAt(LocalDateTime localDateTime) {
        deletedAt = localDateTime;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void changeClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void changeStatus(DiscussionStatus status) {
        this.status = status;
    }

    public void changeId(Long id) {
        this.id = id;
    }

    public void changeParticipants(
        List<DiscussionParticipant> participants) {
        this.participants = participants;
    }

    public void changeConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public void changeResult(Boolean result) {
        this.result = result;
    }

    public void changeAgreePercent(Integer agreePercent) {
        this.agreePercent = agreePercent;
    }

    public void changeDisagreePercent(Integer disagreePercent) {
        this.disagreePercent = disagreePercent;
    }

    public void changeReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
