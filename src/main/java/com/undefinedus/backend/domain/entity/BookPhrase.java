package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class BookPhrase extends BaseEntity{
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 내용 === //
    @Column(length = 2000, nullable = false)
    private String content;  // 구절 내용
    
    @Column(length = 50)
    private String category;  // 구절 카테고리 (ex. 사랑, 우정, 가족)
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aladin_book_id", nullable = false)
    private AladinBook aladinBook;  // 구절이 포함된 책
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 구절을 저장한 회원
    
    // === Soft Delete 관련 === // 구절에는 소프트 딜리트 필요 없지 않을까
//    @Column(nullable = false)
//    @Builder.Default
//    private boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private LocalDateTime deletedAt;
}
