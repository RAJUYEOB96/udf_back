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
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AladinBook {
    // 알라딘에서 제공받는 api 정보와 맞추기 위해 아래 제약조건들을 최대한 api와 동일하도록 설정
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 책 기본 정보 === //
    @Column(length = 13, nullable = false, unique = true, updatable = false)
    private String isbn13;        // ISBN13은 13자리 고정이므로 적절
    
    @Column(length = 500, nullable = false)  // 제목이 매우 긴 경우가 있음
    private String title;         // 도서 제목
    
    @Column(length = 500)         // 부제목도 긴 경우가 있으며, null 가능
    private String subTitle;      // 부제목
    
    @Column(length = 200, nullable = false)  // 여러 저자가 있을 수 있음 (김재원, 김용)
    private String author;        // 저자
    
    @Column(length = 2000)        // 요약이 없는 책도 있으므로 nullable true
    private String summary;       // 요약
    
    @Column(nullable = false)     // URL 길이 제한 제거
    private String link;          // 도서 상세 링크
    
    @Column(nullable = false)     // URL 길이 제한 제거
    private String cover;         // 표지 이미지 URL
    
    @Column(length = 4000, nullable = false)  // 책 소개가 매우 긴 경우가 많음
    private String description;   // 도서 소개
    
    @Column(length = 100, nullable = false)  // 출판사명은 적절
    private String publisher;     // 출판사
    
    // === 카테고리 정보 === //
    @Column(length = 200, nullable = false)  // 카테고리 경로가 길 수 있음
    private String category;      // 카테고리명
    
    // === 평가 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private Double customerReviewRank = 0.0;  // 리뷰가 없는 경우 0점으로 초기화
    
    // === 부가 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isAdult = false;     // 성인여부 (boolean으로 변경)
    
    @Column                             // 페이지 정보가 없는 책도 있음
    private Integer pagesCount;         // 총 페이지 수
}
