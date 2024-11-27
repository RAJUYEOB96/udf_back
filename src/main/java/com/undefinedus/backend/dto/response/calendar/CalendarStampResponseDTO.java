package com.undefinedus.backend.dto.response.calendar;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.enums.BookStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarStampResponseDTO {
    
    private Long myBookId;
    
    private String bookTitle;        // 책 제목
    
    private String bookAuthor;        // 저자
    
    private String bookCover;
    
    private LocalDate recordedAt;     // 독서 기록 날짜
    
    private BookStatus status;
    
    private Integer itemPage;         // 총 페이지 수
    
    private Integer currentPage;   // 현재 읽은 페이지
    
    private LocalDate startDate;    // 읽기 시작한 날짜
    
    private LocalDate endDate;   // 완독한/멈춘 날짜
    
    private Integer readDateCount;  // readCount 이건 따로 계산해서 넣어줄 예정
    
    private Double myRating;  // 다 읽은 책, 읽고 있는 책 일때 사용할 별점
    
    public static CalendarStampResponseDTO from (CalendarStamp calendarStamp) {
        return CalendarStampResponseDTO.builder()
                .myBookId(calendarStamp.getMyBookId())
                .bookTitle(calendarStamp.getBookTitle())
                .bookAuthor(calendarStamp.getBookAuthor())
                .bookCover(calendarStamp.getBookCover())
                .recordedAt(calendarStamp.getRecordedAt())
                .status(calendarStamp.getStatus())
                .itemPage(calendarStamp.getItemPage())
                .currentPage(calendarStamp.getCurrentPage())
                .startDate(calendarStamp.getStartDate())
                .endDate(calendarStamp.getEndDate())
                .readDateCount(calendarStamp.getReadDateCount())
                .myRating(calendarStamp.getMyRating())
                .build();
    }
}
