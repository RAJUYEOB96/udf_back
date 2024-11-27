package com.undefinedus.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsResponseDTO {
    private StatisticsYearResponseDTO yearlyStats;
    private double monthlyAverageBooks;
    private Long totalBooksThisYear;
    private Long totalPagesThisYear;
}
