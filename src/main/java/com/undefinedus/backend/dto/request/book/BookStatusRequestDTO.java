package com.undefinedus.backend.dto.request.book;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookStatusRequestDTO {

    private Double myRating;

    private String oneLineReview;

    private Integer currentPage;

    private LocalDate startDate;

    private LocalDate finishDate;

}
