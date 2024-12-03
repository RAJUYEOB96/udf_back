package com.undefinedus.backend.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsCategoryBookCountResponseDTO {

    private String categoryName;
    private Long bookCount;
}
