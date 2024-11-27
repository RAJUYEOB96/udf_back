package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryBookCountResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearsBookInfoResponseDTO;
import java.util.List;

public interface StatisticsService {

    StatisticsCategoryResponseDTO getCategoryAndBookCountList(Long memberId);

    StatisticsYearsBookInfoResponseDTO getTotalStatisticsYearsBookInfo(Long memberId);

    StatisticsResponseDTO getMonthCompletedBooksByYear(Integer searchYear, Long memberId);
}
