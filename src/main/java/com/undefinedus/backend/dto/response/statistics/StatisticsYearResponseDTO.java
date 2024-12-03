package com.undefinedus.backend.dto.response.statistics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsYearResponseDTO {

    private Integer year;
    private List<StatisticsMonthBookByYearResponseDTO> monthlyStats;

}
