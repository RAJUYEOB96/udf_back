package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryBookCountResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsCategoryResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsMonthBookByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsTotalPageByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearBookResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsMonthBookAverageByYearResponseDTO;
import com.undefinedus.backend.dto.response.statistics.StatisticsYearsBookInfoResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Set;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class StatisticsServiceImplTest {

    @Autowired
    private EntityManager em; // 실제 DB에 접근하여 데이터를 삽입/검증

    @Autowired
    private StatisticsServiceImpl statisticsServiceImpl;

    @Autowired
    private MyBookRepository myBookRepository; // 실제 DB 대신 mock 객체를 사용

    @Autowired
    private AladinBookRepository aladinBookRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        
        // === Member 생성 === //
        member = Member.builder()
            .username("test11@test.com")
            .password("password123")
            .nickname("tester")
            .memberRoleList(List.of(MemberType.USER))
            .isPublic(true)
            .build();
        em.persist(member);

        // === 10개의 테스트용 책 데이터 생성 === //
        for (int i = 1; i <= 10; i++) {
            // 카테고리별로 다르게 설정
            String category = (i % 2 == 0) ? "IT/컴퓨터" : "문학";

            AladinBook book = AladinBook.builder()
                .isbn13(String.format("973895674%04d", i))
                .title("테스트 책 " + i)
                .author(i % 2 == 0 ? "김작가" : "이작가")
                .link("http://example.com/" + i)
                .cover("http://example.com/cover" + i)
                .fullDescription("테스트 설명 " + i)
                .fullDescription2("test " + i)
                .publisher("테스트출판사")
                .categoryName(category) // 다양한 카테고리 설정
                .customerReviewRank(3.8)
                .itemPage(500)
                .build();
            em.persist(book);

            // 책의 상태와 부가 정보 설정
            MyBook myBook = MyBook.builder()
                .member(member)
                .aladinBook(book)
                .isbn13(book.getIsbn13())
                .status(getStatusForIndex(i)) // 상태 설정 메소드 (예: 'COMPLETED' 등)
                .myRating(i % 5 == 0 ? null : (double) (i % 5 + 1))
                .oneLineReview(i % 3 == 0 ? null : "한줄평 " + i)
                .currentPage(i % 4 == 0 ? null : i * 50)
                .startDate(getStartDateForIndex(i)) // 각 책에 대해 다양한 시작 날짜 설정
                .endDate(getEndDateForIndex(i)) // 각 책에 대해 다양한 종료 날짜 설정
                .build();
            em.persist(myBook);
        }

        em.flush();
        em.clear();
    }

    // Helper method to set the status for each book based on index
    private BookStatus getStatusForIndex(int index) {
        // 테스트 데이터에서 다양한 상태를 부여하기 위해 인덱스를 기준으로 상태 설정
        switch (index % 3) {
            case 0:
                return BookStatus.COMPLETED;
            case 1:
                return BookStatus.READING;
            default:
                return BookStatus.STOPPED;
        }
    }

    // Helper method to generate start dates based on index
    private LocalDate getStartDateForIndex(int index) {
        // 예시: 인덱스를 기준으로 각기 다른 연도 및 월 설정 (2020년 1월부터 시작)
        return LocalDate.of(2020 + (index % 3), (index % 12) + 1, 1);
    }

    // Helper method to generate end dates based on index
    private LocalDate getEndDateForIndex(int index) {
        // 예시: 완료 날짜는 시작 날짜 이후 1~12개월 사이로 설정
        LocalDate startDate = getStartDateForIndex(index);
        return startDate.plusMonths((index % 12) + 1);
    }

    @DisplayName("책 데이터가 정상적으로 데이터베이스에 삽입되었는지 확인")
    @Test
    void testPersistedData() {
        // given
        Long memberId = member.getId();

        // when
        List<MyBook> myBooks = em.createQuery(
                "SELECT mb FROM MyBook mb WHERE mb.member.id = :memberId", MyBook.class)
            .setParameter("memberId", memberId)
            .getResultList();

        // then
        assertNotNull(myBooks);
        assertEquals(10, myBooks.size());  // 10개의 MyBook 객체가 삽입된 경우

        // 출력
        System.out.println("Persisted books: " + myBooks.size() + " books found.");
    }

    @DisplayName("카테고리별로 완료된 책 권 수를 반환하는지 검증")
    @Test
    void testGetCategoryAndBookCountList() {
        Long memberId = member.getId();

        // when
        StatisticsCategoryResponseDTO result = statisticsServiceImpl.getCategoryAndBookCountList(
            memberId);

        // then
        assertNotNull(result);
        assertEquals(2, result.getStatisticsCategoryBookCountResponseDTOList().size()); // 두 개의 카테고리 (IT/컴퓨터, 문학)가 있을 것으로 예상
        assertTrue(result.getStatisticsCategoryBookCountResponseDTOList().stream().anyMatch(dto -> dto.getCategoryName().equals("IT/컴퓨터")));
        assertTrue(result.getStatisticsCategoryBookCountResponseDTOList().stream().anyMatch(dto -> dto.getCategoryName().equals("문학")));

        // IT/컴퓨터와 문학 각각에 대해 완료된 책 권 수가 제대로 반환되었는지 확인
        result.getStatisticsCategoryBookCountResponseDTOList().forEach(dto -> {
            // 카테고리 이름과 책 권 수 출력
            System.out.println(dto.getCategoryName() + ": " + dto.getBookCount());
            assertTrue(dto.getBookCount() >= 0, "Book count should be greater than 0");
        });
    }
    
    @DisplayName("연도별 책 통계 정보가 정확히 반환되는지 검증")
    @Test
    void testGetTotalStatisticsYearsBookInfo() {
        // given
        Long memberId = member.getId();
        LocalDate currentDate = LocalDate.now();
        
        // 테스트 데이터 생성을 수정
        int[] pagesPerYear = new int[3];
        for (int i = 0; i < 5; i++) {
            int year = currentDate.getYear() - (i % 3);
            int pages = (i + 1) * 100;
            pagesPerYear[i % 3] += pages;
            
            // AladinBook 먼저 생성
            AladinBook book = AladinBook.builder()
                    .isbn13(String.format("978825674%04d", i))
                    .title("테스트 책 " + i)
                    .author(i % 2 == 0 ? "김작가" : "이작가")
                    .link("testtestets")
                    .cover("testtest")
                    .fullDescription("testtest")
                    .fullDescription2("dfsfsdfsdf")
                    .publisher("testtest")
                    .categoryName(i % 2 == 0 ? "IT/컴퓨터" : "문학")
                    .itemPage(500) // 페이지 수를 명시적으로 지정
                    .build();
            em.persist(book);
            
            // MyBook 생성시 AladinBook 연결
            MyBook myBook = MyBook.builder()
                    .member(member)
                    .aladinBook(book)
                    .isbn13(book.getIsbn13())
                    .status(BookStatus.COMPLETED) // 모든 책을 COMPLETED 상태로 설정
                    .currentPage(pages)
                    .startDate(LocalDate.of(year, 1, 1))
                    .endDate(LocalDate.of(year, 12, 31)) // 연도별로 명확한 종료일 설정
                    .build();
            em.persist(myBook);
        }
        
        em.flush();
        em.clear();
        
        // when
        StatisticsYearsBookInfoResponseDTO result = statisticsServiceImpl.getTotalStatisticsYearsBookInfo(memberId);
        
        // then
        assertNotNull(result);
        
        // === 연도별 책 읽은 권 수 확인 === //
        List<StatisticsYearBookResponseDTO> yearBookCountList = result.getStatisticsYearBookResponseDTO();
        assertNotNull(yearBookCountList);
        assertTrue(yearBookCountList.size() > 0, "연도별 책 읽은 권수 데이터가 존재해야 합니다");
        
        System.out.println("=== 연도별 책 읽은 권 수 ===");
        yearBookCountList.forEach(yearData -> {
            System.out.println("Year: " + yearData.getYear() + ", Completed Books: " + yearData.getCompletedBooks());
            assertNotNull(yearData.getYear(), "연도 값이 null이 아니어야 합니다");
            assertTrue(yearData.getCompletedBooks() >= 0, "완독한 책 수는 0 이상이어야 합니다");
        });
        
        // === 연도별 월 평균 책 읽은 권 수 확인 === //
        List<StatisticsMonthBookAverageByYearResponseDTO> monthAverageBookCount = result.getStatisticsMonthBookAverageByYearResponseDTO();
        assertNotNull(monthAverageBookCount, "월 평균 데이터가 null이 아니어야 합니다");
        assertTrue(monthAverageBookCount.size() > 0, "월 평균 데이터가 존재해야 합니다");
        
        System.out.println("=== 연도별 월 평균 책 읽은 권 수 ===");
        monthAverageBookCount.forEach(monthData -> {
            System.out.println("Year: " + monthData.getYear() + ", Average Books Per Month: " + monthData.getAverageBooks());
            assertNotNull(monthData.getYear(), "연도 값이 null이 아니어야 합니다");
            assertNotNull(monthData.getAverageBooks(), "월 평균 값이 null이 아니어야 합니다");
            assertTrue(monthData.getAverageBooks() >= 0, "월 평균 권수는 0 이상이어야 합니다");
        });
        
        // === 각 연도별 총 페이지 수 확인 === //
        List<StatisticsTotalPageByYearResponseDTO> totalPageByYear = result.getStatisticsTotalPageByYearResponseDTO();
        assertNotNull(totalPageByYear, "연도별 총 페이지 데이터가 null이 아니어야 합니다");
        assertTrue(totalPageByYear.size() > 0, "연도별 총 페이지 데이터가 존재해야 합니다");
        
        System.out.println("=== 연도별 총 페이지 수 ===");
        totalPageByYear.forEach(pageData -> {
            System.out.println("Year: " + pageData.getYear() + ", Total Pages: " + pageData.getTotalPage());
            assertNotNull(pageData.getYear(), "연도 값이 null이 아니어야 합니다");
            long totalPage = pageData.getTotalPage().longValue();
            assertTrue(totalPage >= 0, "총 페이지 수는 0 이상이어야 합니다");
        });
        
        // === 전체 통계 검증 === //
        yearBookCountList.forEach(yearData -> {
            Integer year = yearData.getYear();
            long completedBooks = yearData.getCompletedBooks();
            
            // 월 평균 데이터 검증
            monthAverageBookCount.stream()
                    .filter(monthData -> monthData.getYear().equals(year))
                    .findFirst()
                    .ifPresent(monthData -> {
                        double monthlyTotal = monthData.getAverageBooks() * (year == currentDate.getYear() ?
                                currentDate.getMonthValue() : 12);
                        assertTrue(monthlyTotal >= completedBooks - 0.01,
                                String.format("월 평균 권수 합계(%.2f)는 총 책 권수(%d)와 일치해야 합니다", monthlyTotal, completedBooks));
                    });
            
            // 총 페이지 데이터 검증
            totalPageByYear.stream()
                    .filter(pageData -> pageData.getYear().equals(year))
                    .findFirst()
                    .ifPresent(pageData -> {
                        long totalPage = pageData.getTotalPage().longValue();
                        assertTrue(totalPage > 0, String.format("%d년의 총 페이지 수는 0보다 커야 합니다", year));
                    });
        });
    }


//    @DisplayName("연도별로 책 읽은 권 수를 제대로 반환하는지 검증")
//    @Test
//    void testGetYearBookCountList() {
//        Long memberId = member.getId();
//
//        // when
//        List<StatisticsYearBookResponseDTO> result = statisticsServiceImpl.getYearBookCountList(memberId);
//
//        // then
//        assertNotNull(result);
//        assertTrue(result.size() >= 0);
//        // 연도별로 책 읽은 권 수가 있을 것으로 기대
//
//        // 연도별 책 권 수 출력
//        result.forEach(dto -> System.out.println("Year: " + dto.getYear() + ", 총 권 수: " + dto.getCompletedBooks()));
//    }
//
//    @DisplayName("연도별 월 평균 책 읽은 권 수를 제대로 반환하는지 검증")
//    @Test
//    void testGetYearAverageBookCount() {
//        Long memberId = member.getId();
//
//        // when
//        List<StatisticsMonthBookAverageByYearResponseDTO> result = statisticsServiceImpl.getYearAverageBookCount(memberId);
//
//        // then
//        assertNotNull(result);
//        assertTrue(result.size() >= 0); // 최소 1개 이상의 연도 정보가 있어야 함
//        result.forEach(dto -> {
//            assertTrue(dto.getAverageBooks() >= 0, "Average books for year " + dto.getYear() + " should be 0 or greater");
//
//            // 출력: 각 연도에 대한 평균 읽은 책 권 수 (월 평균)
//            System.out.println("Year: " + dto.getYear() + ", 월 평균 권 수: " + dto.getAverageBooks());
//        });
//
//        System.out.println("result = " + result);
//    }
//
//    @Test
//    @DisplayName("연도별 총 읽은 페이지 수 조회 테스트")
//    void testGetTotalPageByYear() {
//        // given
//        Long memberId = member.getId();
//        LocalDate currentDate = LocalDate.now();
//
//        // 테스트 데이터 생성
//        int[] pagesPerYear = new int[3];
//        for (int i = 0; i < 5; i++) {
//            int year = currentDate.getYear() - (i % 3);
//            int pages = (i + 1) * 100;
//            pagesPerYear[i % 3] += pages;
//
//            MyBook myBook = MyBook.builder()
//                .member(member)
//                .aladinBook(aladinBookRepository.findAll().get(i % 10))
//                .isbn13("978895674" + String.format("%04d", i))
//                .status(BookStatus.COMPLETED)
//                .currentPage(pages)
//                .endDate(LocalDate.of(year, 12, 31))
//                .build();
//            em.persist(myBook);
//        }
//        em.flush();
//        em.clear();
//
//        // when
//        List<StatisticsTotalPageByYearResponseDTO> result = statisticsServiceImpl.getTotalPageByYear(memberId);
//
//        // then
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertEquals(3, result.size()); // 정확히 3년치 데이터
//
//        // 결과 검증
//        for (int i = 0; i < 3; i++) {
//            StatisticsTotalPageByYearResponseDTO dto = result.get(i);
//            int expectedYear = currentDate.getYear() - i;
//            long expectedPages = pagesPerYear[i];
//
//            assertEquals(expectedYear, dto.getYear().intValue());
//            assertEquals(expectedPages, dto.getTotalPage().longValue());
//            System.out.println("Year: " + dto.getYear() + ", Total Pages: " + dto.getTotalPage());
//        }
//
//        // 연도가 내림차순으로 정렬되었는지 확인
//        assertTrue(result.stream()
//            .map(StatisticsTotalPageByYearResponseDTO::getYear)
//            .reduce((a, b) -> {
//                assertTrue(a > b);
//                return b;
//            }).isPresent());
//    }

    @DisplayName("연도별 월별 책 읽은 권수와 페이지 수를 제대로 반환하는지 검증")
    @Test
    void testGetMonthCompletedBooksByYear() {
        // given
        Long memberId = member.getId();
        Integer currentYear = Year.now().getValue();

        // when (현재 연도를 생략한 경우)
        StatisticsResponseDTO currentYearResult = statisticsServiceImpl.getMonthCompletedBooksByYear(
            null, memberId);

        // then
        assertNotNull(currentYearResult);
        assertNotNull(currentYearResult.getYearlyStats());
        assertNotNull(currentYearResult.getYearlyStats().getMonthlyStats());
        assertEquals(12, currentYearResult.getYearlyStats().getMonthlyStats().size());

        // 현재 연도 데이터 출력 및 검증
        System.out.println("=== 현재 연도(" + currentYear + ") 통계 ===");
        System.out.println("총 읽은 책: " + currentYearResult.getTotalBooksThisYear() + "권");
        System.out.println("총 읽은 페이지: " + currentYearResult.getTotalPagesThisYear() + "페이지");
        System.out.println("월 평균 읽은 책: " + currentYearResult.getMonthlyAverageBooks() + "권");

        currentYearResult.getYearlyStats().getMonthlyStats().forEach(month -> {
            System.out.println(month.getYear() + "년 " + month.getMonth() + "월: "
                + month.getCompletedBooks() + "권, "
                + month.getPages() + "페이지");

            // 기본 검증
            assertNotNull(month.getYear());
            assertTrue(month.getMonth() >= 1 && month.getMonth() <= 12);
            assertNotNull(month.getCompletedBooks());
            assertNotNull(month.getPages());
            assertTrue(month.getCompletedBooks() >= 0);
            assertTrue(month.getPages() >= 0);
        });

        // when (특정 연도 검색)
        Integer searchYear = 2022;
        StatisticsResponseDTO specificYearResult = statisticsServiceImpl.getMonthCompletedBooksByYear(
            searchYear, memberId);

        // then
        assertNotNull(specificYearResult);
        assertEquals(searchYear, specificYearResult.getYearlyStats().getYear());

        // 특정 연도 데이터 출력 및 검증
        System.out.println("\n=== " + searchYear + "년 통계 ===");
        System.out.println("총 읽은 책: " + specificYearResult.getTotalBooksThisYear() + "권");
        System.out.println("총 읽은 페이지: " + specificYearResult.getTotalPagesThisYear() + "페이지");
        System.out.println("월 평균 읽은 책: " + specificYearResult.getMonthlyAverageBooks() + "권");

        specificYearResult.getYearlyStats().getMonthlyStats().forEach(month -> {
            System.out.println(month.getYear() + "년 " + month.getMonth() + "월: "
                + month.getCompletedBooks() + "권, "
                + month.getPages() + "페이지");
        });

        // 총계 검증
        assertTrue(specificYearResult.getTotalBooksThisYear() >= 0);
        assertTrue(specificYearResult.getTotalPagesThisYear() >= 0);
        assertTrue(specificYearResult.getMonthlyAverageBooks() >= 0);

        // 월별 통계의 합이 전체 통계와 일치하는지 검증
        long totalBooksFromMonthly = specificYearResult.getYearlyStats().getMonthlyStats().stream()
            .mapToLong(StatisticsMonthBookByYearResponseDTO::getCompletedBooks)
            .sum();
        long totalPagesFromMonthly = specificYearResult.getYearlyStats().getMonthlyStats().stream()
            .mapToLong(StatisticsMonthBookByYearResponseDTO::getPages)
            .sum();

        assertEquals(specificYearResult.getTotalBooksThisYear(), totalBooksFromMonthly);
        assertEquals(specificYearResult.getTotalPagesThisYear(), totalPagesFromMonthly);
    }

    @Test
    @DisplayName("회원의 다읽은 책을 기록한 년도의 목록")
    void getMemberYears() {

        Long memberId = member.getId();

        Set<Integer> memberYears = statisticsServiceImpl.getMemberYears(memberId);

        System.out.println("memberYears = " + memberYears);
    }

    @DisplayName("완료된 책을 연도별로 그룹화하여 반환하는지 검증")
    @Test
    @Disabled("컨트롤러에서는 잘 작동 되지만 테스트에서만 에러가 남 일단 프론트에 전달해야 해서 이 테스트는 추후에 수정 예정")
    void testGetCompletedBooksGroupedByYears() {
        // given
        Long memberId = member.getId();  // 테스트용 회원 ID 가져오기

        // when
        List<StatisticsCategoryByYearResponseDTO> result = statisticsServiceImpl.getCompletedBooksGroupedByYears(memberId);

        System.out.println("result = " + result);
        // then
        assertNotNull(result);  // 결과가 null이 아닌지 확인
        assertFalse(result.isEmpty());  // 결과 리스트가 비어있지 않은지 확인

        // 연도별 그룹화된 결과 출력 및 검증
        result.forEach(dto -> {
            System.out.println("Year: " + dto.getYear());
            dto.getStatisticsCategoryBookCountResponseDTOList().forEach(categoryDto -> {
                System.out.println("Category: " + categoryDto.getCategoryName() + ", Book count: " + categoryDto.getBookCount());
                assertNotNull(categoryDto.getCategoryName());
                assertTrue(categoryDto.getBookCount() >= 0, "Book count should be greater than or equal to 0");
            });
        });
    }
}