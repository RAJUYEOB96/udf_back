package com.undefinedus.backend.dto.request.book;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class BookStatusRequestDTO {
    
    private String status; // COMPLETED, WISH, READING, STOPPED
    
    private Double myRating;
    
    private String oneLineReview;
    
    private Integer currentPage;
    
    @Builder.Default
    private Integer updateCount = 0; // updateCount (React에서 계산해서 넘어오는 값)
    
    private LocalDate startDate;
    
    private LocalDate endDate;

}
