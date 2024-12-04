package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_discussion_participant", // 제약조건 이름 지정
            columnNames = {"discussion_id", "member_id"} // 한 토론에 한 멤버는 한번만 참여 가능
        )
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"discussion", "member"})  // 순환참조 방지
public class DiscussionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private boolean isAgree;  // true: 찬성, false: 반대

}
