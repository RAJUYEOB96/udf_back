package com.undefinedus.backend.dto.response.statistics;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsMonthBookByYearResponseDTO {

    private Integer year;             // 연도
    private Integer month;
    private Long completedBooks;    // 읽은 책 권수
    private Long pages;  // 읽은 책의 페이지 수
}
