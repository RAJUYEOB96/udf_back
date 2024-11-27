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
    
    private Long id;
    
    // 책 상태 정보
    private String status;
    
    private Double myRating;
    
    private String oneLineReview;
    
    private Integer currentPage;
    
    private Integer updateCount; // (React에서 넘어오는 값)
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Builder.Default
    private Integer readDateCount = 0; // 따로 넣어줄 예정 CalendarStamp에서 계산해서 (기록 횟수 카운트)
    
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
    
    // 아래는 social에서 남의 기록한 책을 볼때 필요한 필드
    
    // 내가 남의 책장을 볼때 null이면 기록안된 책
    // 기록되어있으면 "COMPLETED", "READING", "WISH", "STOPPED" 중 하나
    private String existingStatus;
    
    
    public static MyBookResponseDTO from(MyBook myBook, Integer count) {
        return from(myBook, count, null);
    }
    
    public static MyBookResponseDTO from(MyBook myBook, Integer count, String existingStatus) {
        return MyBookResponseDTO.builder()
                .id(myBook.getId())
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
                .customerReviewRank(myBook.getAladinBook().getCustomerReviewRank())
                .itemPage(myBook.getAladinBook().getItemPage())
                .existingStatus(existingStatus)
                .build();
    }
    
    
}
