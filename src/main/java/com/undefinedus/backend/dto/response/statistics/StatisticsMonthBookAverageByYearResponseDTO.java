package com.undefinedus.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsMonthBookAverageByYearResponseDTO {

    private Integer year;                // 연도
    private Double averageBooks;     // 연도별 평균 책 권수
}
