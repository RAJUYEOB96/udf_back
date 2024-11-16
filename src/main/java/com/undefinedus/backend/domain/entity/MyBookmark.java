package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyBookmark extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 연관 관계 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aladin_book_id", nullable = false)
    private AladinBook aladinBook;  // 구절이 포함된 책 - 책 제목(메인 + 서브)같은 것을 뽑아쓰기 위해
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 구절을 저장한 회원
    
    
    // === 내용 === //
    @Column(length = 200, nullable = false)
    private String phrase;  // 구절 내용
    
    @Column
    private int pageNumber;  // 구절이 있는 페이지
    

}
