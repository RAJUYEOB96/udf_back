package com.undefinedus.backend.dto.response.statistics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsYearsBookInfoResponseDTO {

    // 연도 별 책 권 수
    private List<StatisticsYearBookResponseDTO> statisticsYearBookResponseDTO;
    // 연도 별 월 평균 책 권 수
    private List<StatisticsMonthBookAverageByYearResponseDTO> statisticsMonthBookAverageByYearResponseDTO;
    // 연도 별 총 페이지 수
    private List<StatisticsTotalPageByYearResponseDTO> statisticsTotalPageByYearResponseDTO;
}
