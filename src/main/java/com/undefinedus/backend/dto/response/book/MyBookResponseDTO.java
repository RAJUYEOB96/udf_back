package com.undefinedus.backend.dto.response.book;

import com.undefinedus.backend.domain.entity.MyBook;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyBookResponseDTO {
    
    // 책 상태 정보
    private String status;
    
    private Double myRating;
    
    private String oneLineReview;
    
    private Integer currentPage;
    
    private Integer updateCount; // (React에서 넘어오는 값)
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Builder.Default
    private Integer readDateCount = 0; // 따로 넣어줄 예정 CalendarStamp에서 계산해서  (읽은 횟수 카운트)
    
    // 책 정보 (AladinBook에서 뽑아올 예정)
    
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
    
    
    public static MyBookResponseDTO from(MyBook myBook, Integer count) {
        return MyBookResponseDTO.builder()
                .status(myBook.getStatus().name())
                .myRating(myBook.getMyRating())
                .oneLineReview(myBook.getOneLineReview())
                .currentPage(myBook.getCurrentPage())
                .updateCount(myBook.getUpdateCount())
                .startDate(myBook.getStartDate())
                .endDate(myBook.getEndDate())
                .readDateCount(count)
                .isbn13(myBook.getIsbn13())
                .title(myBook.getAladinBook().getTitle())
                .author(myBook.getAladinBook().getAuthor())
                .link(myBook.getAladinBook().getLink())
                .cover(myBook.getAladinBook().getCover())
                .fullDescription(myBook.getAladinBook().getFullDescription())
                .publisher(myBook.getAladinBook().getPublisher())
                .categoryName(myBook.getAladinBook().getCategoryName())
                .itemPage(myBook.getAladinBook().getItemPage())
                .build();
    }
    
    public void updateUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }
}
