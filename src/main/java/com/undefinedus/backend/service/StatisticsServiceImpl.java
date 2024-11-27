package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryBookCountResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsMonthBookAverageByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsMonthBookByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsTotalPageByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearBookResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearsBookInfoResponseDTO;
import com.undefinedus.backend.repository.MyBookRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MyBookRepository myBookRepository;

    // 카테고리별 읽은 책 권 수
    public StatisticsCategoryResponseDTO getCategoryAndBookCountList(Long memberId) {

        List<Object[]> results = myBookRepository.findCompletedBooksGroupedByCategory(memberId);

        List<StatisticsCategoryBookCountResponseDTO> statisticsCategoryBookCountResponseDTOList = results.stream()
            .map(result -> {

                String categoryName = (String) result[0];
                String[] parts = categoryName.split(">");

                // 두 번째 항목 가져오기 (배열 길이 체크 추가)
                String secondCategory = parts.length > 1 ? parts[1] : categoryName;

                return StatisticsCategoryBookCountResponseDTO.builder()
                .categoryName(secondCategory.trim())
                .bookCount((Long) result[1])
                .build();
            })
            .collect(Collectors.toList());

        // 총 책 권수 계산
        Long totalBookCount = statisticsCategoryBookCountResponseDTOList.stream()
            .mapToLong(StatisticsCategoryBookCountResponseDTO::getBookCount)
            .sum();

        StatisticsCategoryResponseDTO statisticsCategoryResponseDTO = StatisticsCategoryResponseDTO.builder()
            .totalCount(totalBookCount)
            .statisticsCategoryBookCountResponseDTOList(statisticsCategoryBookCountResponseDTOList)
            .build();

        return statisticsCategoryResponseDTO;
    }

    // 연도 별 읽은 책 권 수
    // 연도 별 월 평균 책 읽은 권 수
    // 각 연도 별 총 페이지 수
    // 다 합친 것
    public StatisticsYearsBookInfoResponseDTO getTotalStatisticsYearsBookInfo(Long memberId) {

        List<StatisticsYearBookResponseDTO> yearBookCountList = getYearBookCountList(memberId);
        List<StatisticsMonthBookAverageByYearResponseDTO> monthAverageBookCount = getYearAverageBookCount(
            memberId);
        List<StatisticsTotalPageByYearResponseDTO> totalPageByYear = getTotalPageByYear(memberId);

        StatisticsYearsBookInfoResponseDTO statisticsYearsBookInfoResponseDTO = StatisticsYearsBookInfoResponseDTO.builder()
            .statisticsMonthBookAverageByYearResponseDTO(monthAverageBookCount)
            .statisticsTotalPageByYearResponseDTO(totalPageByYear)
            .statisticsYearBookResponseDTO(yearBookCountList)
            .build();

        return statisticsYearsBookInfoResponseDTO;
    }

    // 연도 별 읽은 책 권 수 // 그래프에도 쓰임
    private List<StatisticsYearBookResponseDTO> getYearBookCountList(Long memberId) {

        List<Object[]> results = myBookRepository.findCompletedBooksGroupedByYear(
            memberId);

        List<StatisticsYearBookResponseDTO> statisticsYearBookResponseDTOList = results.stream()
            .map(result -> StatisticsYearBookResponseDTO.builder()
                .year((Integer) result[0])
                .completedBooks((Long) result[1])
                .build())
            .collect(Collectors.toList());

        return statisticsYearBookResponseDTOList;
    }

    // 연도 별 월 평균 책 읽은 권 수
    private List<StatisticsMonthBookAverageByYearResponseDTO> getYearAverageBookCount(
        Long memberId) {
        List<StatisticsYearBookResponseDTO> yearBookCounts = getYearBookCountList(memberId);

        // 연도별로 월 평균 책 권 수 계산
        List<StatisticsMonthBookAverageByYearResponseDTO> result = yearBookCounts.stream()
            .map(dto -> {
                double average = dto.getCompletedBooks() > 0 ? dto.getCompletedBooks() / 12.0 : 0.0;
                return StatisticsMonthBookAverageByYearResponseDTO.builder()
                    .year(dto.getYear())
                    .averageBooks(Double.parseDouble(String.format("%.1f", average)))
                    .build();
            })
            .collect(Collectors.toList());

        return result;
    }

    // 각 연도 별 총 페이지 수
    private List<StatisticsTotalPageByYearResponseDTO> getTotalPageByYear(Long memberId) {
        List<Object[]> results = myBookRepository.finCompletedBookPageGroupedByYear(memberId);

        List<StatisticsTotalPageByYearResponseDTO> responseDTOList = results.stream().map(
            result -> {
                return StatisticsTotalPageByYearResponseDTO.builder()
                    .year((Integer) result[0])
                    .totalPage(convertToLong(result[1]))
                    .build();
            }).collect(Collectors.toList());

        return responseDTOList;
    }

    private Long convertToLong(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        } else {
            return Long.valueOf(value.toString());
        }
    }

    // 검색한 연도의 월 별 읽은 책 권 수 // 연도를 입력하지 않으면 현재 연도를 나오게 함
    public StatisticsResponseDTO getMonthCompletedBooksByYear(Integer searchYear,
        Long memberId) {

        // 검색 연도가 없으면 현재 연도 사용
        Integer year = searchYear != null ? searchYear : Year.now().getValue();

        // MyBook 리스트 조회
        List<MyBook> books = myBookRepository.findCompletedBooksByYear(year, memberId);

        // 월별 통계 초기화 (1-12월)
        List<StatisticsMonthBookByYearResponseDTO> monthlyStats = initializeMonthlyStats(year);

        // 전체 통계 계산
        long totalBooks = 0;
        long totalPages = 0;

        // 월별 데이터 집계
        Map<Integer, List<MyBook>> booksByMonth = books.stream()
            .collect(Collectors.groupingBy(
                book -> book.getEndDate().getMonthValue()
            ));

        // 각 월별 데이터 설정
        booksByMonth.forEach((month, monthBooks) -> {
            StatisticsMonthBookByYearResponseDTO monthStat = monthlyStats.get(month - 1);
            monthStat.setCompletedBooks((long) monthBooks.size());
            monthStat.setPages(monthBooks.stream()
                .mapToLong(book -> book.getCurrentPage() != null ? book.getCurrentPage() : 0)
                .sum());
        });

        // 전체 통계 계산
        totalBooks = books.size();
        totalPages = books.stream()
            .mapToLong(MyBook::getCurrentPage)
            .sum();

        // 연간 통계 DTO 생성
        StatisticsYearResponseDTO yearStats = StatisticsYearResponseDTO.builder()
            .year(year)
            .monthlyStats(monthlyStats)
            .build();

        // 최종 응답 DTO 생성
        return StatisticsResponseDTO.builder()
            .yearlyStats(yearStats)
            .monthlyAverageBooks(Math.round((double) totalBooks / 12 * 10.0) / 10.0) // 소수점 첫째자리까지
            .totalBooksThisYear(totalBooks)
            .totalPagesThisYear(totalPages)
            .build();
    }

    private List<StatisticsMonthBookByYearResponseDTO> initializeMonthlyStats(Integer year) {
        List<StatisticsMonthBookByYearResponseDTO> monthlyStats = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthlyStats.add(StatisticsMonthBookByYearResponseDTO.builder()
                .year(year)
                .month(month)
                .completedBooks(0L)
                .pages(0L)
                .build());
        }
        return monthlyStats;
    }

}
