package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryBookCountResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryByYearResponseDTO;
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
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private final MyBookRepository myBookRepository;

    // 카테고리별 읽은 책 권 수
    @Override
    public StatisticsCategoryResponseDTO getCategoryAndBookCountList(Long memberId) {

        List<Object[]> results = myBookRepository.findCompletedBooksGroupedByCategory(memberId);

        // 두 번째 카테고리를 기준으로 묶고 카운트 합산
        Map<String, Long> categoryCountMap = results.stream()
            .collect(Collectors.groupingBy(
                result -> {
                    String categoryName = (String) result[0];
                    String[] parts = categoryName.split(">");
                    return parts.length > 1 ? parts[1].trim() : categoryName.trim(); // 두 번째 카테고리 반환
                },
                Collectors.summingLong(result -> (Long) result[1]) // 카운트 합산
            ));

        // Map을 DTO 리스트로 변환
        List<StatisticsCategoryBookCountResponseDTO> statisticsCategoryBookCountResponseDTOList = categoryCountMap.entrySet().stream()
            .map(entry -> StatisticsCategoryBookCountResponseDTO.builder()
                .categoryName(entry.getKey())
                .bookCount(entry.getValue())
                .build())
            .collect(Collectors.toList());

        // 총 책 권수 계산
        Long totalBookCount = statisticsCategoryBookCountResponseDTOList.stream()
            .mapToLong(StatisticsCategoryBookCountResponseDTO::getBookCount)
            .sum();

        // 최종 DTO 생성
        StatisticsCategoryResponseDTO statisticsCategoryResponseDTO = StatisticsCategoryResponseDTO.builder()
            .totalCount(totalBookCount)
            .statisticsCategoryBookCountResponseDTOList(statisticsCategoryBookCountResponseDTOList)
            .build();


        return statisticsCategoryResponseDTO;
    }

    // 최근 3년간의 각 연도의 카테고리 별 다 읽은 책 권 수
    public List<StatisticsCategoryByYearResponseDTO> getCompletedBooksGroupedByYears(Long memberId) {
        // 데이터베이스에서 완료된 책 정보를 가져옴
        List<Object[]> results = myBookRepository.findCompletedBooksGroupedByYears(memberId);

        // 데이터를 연도별로 그룹화
        Map<Integer, List<StatisticsCategoryBookCountResponseDTO>> groupedData = results.stream()
            .collect(Collectors.groupingBy(
                row -> ((Number) row[1]).intValue(), // 연도 추출 (row[1]은 연도 정보)
                Collectors.mapping(row -> {
                    String categoryName = (String) row[0]; // 카테고리 이름 추출
                    String[] parts = categoryName.split(">"); // '>' 기호로 카테고리 분리
                    // 두 번째 카테고리가 있으면 사용, 없으면 전체 카테고리 이름 사용
                    String secondCategory = parts.length > 1 ? parts[1].trim() : categoryName.trim();
                    return new StatisticsCategoryBookCountResponseDTO(
                        secondCategory, // 정제된 카테고리 이름
                        ((Number) row[2]).longValue() // 책 수량 (row[2]는 책 수량 정보)
                    );
                }, Collectors.toList()) // 각 연도별로 카테고리와 책 수량 정보를 리스트로 수집
            ));

        // 그룹화된 데이터를 DTO로 변환하고 카테고리별로 합산
        List<StatisticsCategoryByYearResponseDTO> statisticsCategoryByYearResponseDTOList = groupedData.entrySet().stream()
            .map(entry -> {
                // 각 연도 내에서 카테고리별로 책 수량 합산
                Map<String, Long> categorySum = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                        StatisticsCategoryBookCountResponseDTO::getCategoryName, // 카테고리별 그룹화
                        Collectors.summingLong(StatisticsCategoryBookCountResponseDTO::getBookCount) // 책 수량 합산
                    ));

                // 합산된 결과를 DTO 리스트로 변환
                List<StatisticsCategoryBookCountResponseDTO> summedList = categorySum.entrySet().stream()
                    .map(categoryEntry -> new StatisticsCategoryBookCountResponseDTO(
                        categoryEntry.getKey(), categoryEntry.getValue()))
                    .collect(Collectors.toList());

                // 연도별 통계 DTO 생성
                return StatisticsCategoryByYearResponseDTO.builder()
                    .year(entry.getKey()) // 연도 설정
                    .statisticsCategoryBookCountResponseDTOList(summedList) // 해당 연도의 카테고리별 통계 리스트 설정
                    .build();
            })
            .sorted(Comparator.comparing(StatisticsCategoryByYearResponseDTO::getYear).reversed()) // 연도 기준 내림차순 정렬
            .collect(Collectors.toList()); // 최종 결과를 리스트로 수집

        return statisticsCategoryByYearResponseDTOList; // 처리된 통계 데이터 반환
    }

    // 연도 별 읽은 책 권 수
    // 연도 별 월 평균 책 읽은 권 수
    // 각 연도 별 총 페이지 수
    // 다 합친 것
    @Override
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
        List<Object[]> results = myBookRepository.findCompletedBookPageGroupedByYear(memberId);

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
    @Override
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
                .mapToLong(book -> book.getCurrentPage() != null ? book.getCurrentPage() : book.getAladinBook().getItemPage())
                .sum());
        });

        // 전체 통계 계산
        totalBooks = books.size();
        totalPages = books.stream()
            .mapToLong(book -> {
                log.info(String.valueOf(book.getAladinBook().getItemPage()));
                return book.getCurrentPage() != null ? book.getCurrentPage() : book.getAladinBook().getItemPage();
            })
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

    // 회원의 complete 한 책에서 추출한 year 정보 목록
    public Set<Integer> getMemberYears(Long memberId) {

        Set<Integer> yearsSet = myBookRepository.findByMemberIdByCompleted(memberId);

        return yearsSet;
    }

}
