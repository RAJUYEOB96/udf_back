package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AladinBook{
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 책 기본 정보 === //
    @Column(length = 13, nullable = false, unique = true, updatable = false)
    private String isbn13;        // ISBN13
    
    @Column(length = 200, nullable = false)
    private String title;         // 도서 제목
    
    @Column(length = 200)
    private String subTitle;      // 부제목 (있는 경우)
    
    @Column(length = 100, nullable = false)
    private String author;        // 저자
    
    @Column(length = 200, nullable = false)
    private String link;          // 도서 상세 링크
    
    @Column(length = 200, nullable = false)
    private String cover;         // 표지 이미지 URL
    
    @Column(length = 2000, nullable = false)
    private String description;   // 도서 소개
    
    @Column(length = 100, nullable = false)
    private String publisher;     // 출판사
    
    // === 카테고리 정보 === //
    @Column(length = 100)
    private String category;  // 카테고리명
    
    // === 평가 정보 === //
    @Column(nullable = false)
    private Double customerReviewRank;  // 고객평점
    
    // === 부가 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isAdult = false;     // 성인여부 (boolean으로 변경)
    
    @Column(nullable = false)
    private Integer pagesCount;    // 총 페이지 수

}
