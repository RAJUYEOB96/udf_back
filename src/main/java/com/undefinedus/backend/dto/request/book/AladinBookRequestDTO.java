package com.undefinedus.backend.dto.request.book;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AladinBookRequestDTO {

    private String isbn13;        // ISBN13

    private String title;         // 도서 제목

    private String author;        // 저자

    private String link;          // 도서 상세 링크

    private String cover;         // 표지 이미지 URL

    private String fullDescription;   // 도서 소개

    private String publisher;     // 출판사

    private String categoryName;  // 카테고리명

    private Double customerReviewRank;  // 고객평점

    private Integer itemPage;    // 총 페이지 수


}
