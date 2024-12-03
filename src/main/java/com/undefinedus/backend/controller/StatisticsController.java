package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearsBookInfoResponseDTO;
import com.undefinedus.backend.service.StatisticsService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping // 카테고리별 읽은 책 권 수, 총 권 수
    public ResponseEntity<ApiResponseDTO<StatisticsCategoryResponseDTO>> getCategoryAndBookCountList(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        StatisticsCategoryResponseDTO categoryAndBookCount = statisticsService.getCategoryAndBookCountList(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success((categoryAndBookCount)));
    }

    @GetMapping("/yearly") // 연도 별의 카테고리 별 다 읽은 책 권 수
    public ResponseEntity<ApiResponseDTO<List<StatisticsCategoryByYearResponseDTO>>> getCategoryAndBookCountByYearList(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        List<StatisticsCategoryByYearResponseDTO> completedBooksGroupedByYear = statisticsService.getCompletedBooksGroupedByYears(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success((completedBooksGroupedByYear)));
    }

    @GetMapping("/totalYearly") // 연도별 총 권 수, 월 평균 권 수, 총 페이지 수
    public ResponseEntity<ApiResponseDTO<StatisticsYearsBookInfoResponseDTO>> getTotalStatisticsYearsBookInfo(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        StatisticsYearsBookInfoResponseDTO statisticsYearsBookInfoResponseDTO = statisticsService.getTotalStatisticsYearsBookInfo(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success(statisticsYearsBookInfoResponseDTO));
    }

    @GetMapping("/monthly") // 검색한 년도(기본 값 현재 연도)의 1 ~ 12월 까지의 COMPLETED 책 정보
    public ResponseEntity<ApiResponseDTO<StatisticsResponseDTO>> getMonthlyCompletedMyBookData(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("year") Integer year) {

        Long loginMemberId = memberSecurityDTO.getId();

        StatisticsResponseDTO result = statisticsService.getMonthCompletedBooksByYear(
            year, loginMemberId);

        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    // 검색한 연도의 다 읽은 책의 권 수, 평규 권 수, 총 페이지 수
    @GetMapping("/years")
    public ResponseEntity<ApiResponseDTO<Set<Integer>>> getMemberYears(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {

        Long loginMemberId = memberSecurityDTO.getId();

        Set<Integer> memberYears = statisticsService.getMemberYears(loginMemberId);

        return ResponseEntity.ok(ApiResponseDTO.success(memberYears));
    }
}