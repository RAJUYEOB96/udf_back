package com.undefinedus.backend.dto.response.statistics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsCategoryResponseDTO {

    private Long totalCount;
    private List<StatisticsCategoryBookCountResponseDTO> statisticsCategoryBookCountResponseDTOList;
}
