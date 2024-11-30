package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearsBookInfoResponseDTO;
import java.util.List;
import java.util.Set;

public interface StatisticsService {

    StatisticsCategoryResponseDTO getCategoryAndBookCountList(Long memberId);

    StatisticsYearsBookInfoResponseDTO getTotalStatisticsYearsBookInfo(Long memberId);

    StatisticsResponseDTO getMonthCompletedBooksByYear(Integer searchYear, Long memberId);

    Set<Integer> getMemberYears(Long memberId);

    List<StatisticsCategoryByYearResponseDTO> getCompletedBooksGroupedByYears(Long memberId);
}
