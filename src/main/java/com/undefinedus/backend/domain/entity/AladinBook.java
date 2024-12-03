package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.sql.Clob;
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
    
    @Column(length = 200, nullable = false)  // 여러 저자가 있을 수 있음 (김재원, 김용)
    private String author;        // 저자
    
    @Column(nullable = false)     // URL 길이 제한 제거
    private String link;          // 도서 상세 링크
    
    @Column(nullable = false)     // URL 길이 제한 제거
    private String cover;         // 표지 이미지 URL
    
    @Column(length = 4000, nullable = false)  // 책 소개가 매우 긴 경우가 많음
    private String fullDescription;   // 도서 소개

    @Column(columnDefinition = "LONGTEXT")
    @Lob // 책 소개가 매우 긴 경우가 많음 // 대용량의 텍스트 일때 사용하는 어노테이션
    private String fullDescription2;   // 출판사 제공 책소개  // gpt에게 책의 추가적인 정보를 넘겨주기 위해 필요
    
    @Column(length = 100, nullable = false)  // 출판사명은 적절
    private String publisher;     // 출판사
    
    // === 카테고리 정보 === //
    @Column(length = 200, nullable = false)  // 카테고리 경로가 길 수 있음
    private String categoryName;      // 카테고리명
    
    // === 평가 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private Double customerReviewRank = 0.0;  // 리뷰가 없는 경우 0점으로 초기화

    @Column                             // 페이지 정보가 없는 책도 있음
    private Integer itemPage;         // 총 페이지 수

    public void changeIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public void changeCover(String cover) {
        this.cover = cover;
    }

    public void changeTitle(String title) {
        this.title = title;
    }
}
