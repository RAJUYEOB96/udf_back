package com.undefinedus.backend.dto.response.statistics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsCategoryByYearResponseDTO {

    private Integer year;
    private List<StatisticsCategoryBookCountResponseDTO> statisticsCategoryBookCountResponseDTOList;
}
