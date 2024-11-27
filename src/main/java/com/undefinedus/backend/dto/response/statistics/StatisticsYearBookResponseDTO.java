package com.undefinedus.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsYearBookResponseDTO {

    private Integer year;             // 연도
    private Long completedBooks;  // 해당 연도의 COMPLETED 상태 책 수
}
