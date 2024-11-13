package com.undefinedus.backend.dto.response.book;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponseDTO {

    // 책 상태 정보
    private String status;

    private Double myRating;

    private String oneLineReview;

    private Integer currentPage;

    private LocalDate startDate;

    private LocalDate finishDate;

    private List<LocalDate> readDates = new ArrayList<>();

    // 책 정보
    private String isbn13;        // ISBN13

    private String title;         // 도서 제목

    private String subTitle;      // 부제목 (있는 경우)

    private String author;        // 저자

    private String summary;       // 요약

    private String link;          // 도서 상세 링크

    private String cover;         // 표지 이미지 URL

    private String description;   // 도서 소개

    private String publisher;     // 출판사

    private String category;  // 카테고리명

    private Double customerReviewRank;  // 고객평점

    @Builder.Default
    private boolean isAdult = false;     // 성인여부 (boolean으로 변경)

    private Integer pagesCount;    // 총 페이지 수
}
