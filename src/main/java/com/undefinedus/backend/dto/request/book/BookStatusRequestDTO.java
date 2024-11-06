package com.undefinedus.backend.dto.request.book;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookStatusRequestDTO {

    private String status;

    private Double myRating;

    private String oneLineReview;

    private Integer currentPage;

    private LocalDate startDate;

    private LocalDate finishDate;

    private List<LocalDate> readDates = new ArrayList<>();
}
