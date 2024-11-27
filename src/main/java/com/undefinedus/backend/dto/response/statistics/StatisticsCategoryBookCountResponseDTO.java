package com.undefinedus.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsCategoryBookCountResponseDTO {

    private String categoryName;
    private Long bookCount;
}
