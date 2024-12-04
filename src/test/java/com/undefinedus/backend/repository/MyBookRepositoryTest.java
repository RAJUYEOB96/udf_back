package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MyBookRepositoryTest {

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    @Autowired
    private AladinBookRepository aladinBookRepository;

    @BeforeEach
    void setUp() {
        // === Member 생성 === //
        member = Member.builder()
            .username("test5@test.com")
            .password("password123")
            .nickname("tester")
            .memberRoleList(List.of(MemberType.USER))
            .isPublic(true)
            .build();
        em.persist(member);

        // === 10개의 테스트용 책 데이터 생성 === //
        for (int i = 1; i <= 10; i++) {
            AladinBook book = AladinBook.builder()
                .isbn13(String.format("972895674%04d", i))
                .title("테스트 책 " + i)
                .author(i % 2 == 0 ? "김작가" : "이작가")
                .link("http://example.com/" + i)
                .cover("http://example.com/cover" + i)
                .fullDescription("테스트 설명 " + i)
                .fullDescription2("test " + i)
                .publisher("테스트출판사")
                .categoryName("IT/컴퓨터")
                .customerReviewRank(3.8)
                .itemPage(500)
                .build();
            em.persist(book);

            // 책의 상태와 부가 정보 설정
            MyBook myBook = MyBook.builder()
                .member(member)
                .aladinBook(book)
                .isbn13(book.getIsbn13())
                .status(getStatusForIndex(i))
                .myRating(i % 5 == 0 ? null : (double) (i % 5 + 1))
                .oneLineReview(i % 3 == 0 ? null : "한줄평 " + i)
                .currentPage(i % 4 == 0 ? null : i * 50)
                .startDate(i % 2 == 0 ? LocalDate.now().minusDays(i) : null)
                .endDate(i % 4 == 0 ? LocalDate.now() : null)
                .build();
            em.persist(myBook);

        }



        em.flush();
        em.clear();
    }

    // 인덱스에 따라 다른 상태값 반환
    private BookStatus getStatusForIndex(int i) {
        return switch (i % 4) {
            case 0 -> BookStatus.COMPLETED;
            case 1 -> BookStatus.READING;
            case 2 -> BookStatus.WISH;
            default -> BookStatus.STOPPED;
        };
    }

    @Nested
    @DisplayName("도서 목록 무한 스크롤 조회 테스트")
    class FindBooksWithScrollTest {

        @Test
        @DisplayName("기본 조회 - 첫 페이지, 내림차순")
        void findFirstPageDescTest() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .size(3)         // 페이지당 3개씩
                .sort("desc")    // 내림차순 정렬
                .status(null)    // status 필터 제거 (null 이면 전체조회)
                .build();

            // when
            List<MyBook> result = myBookRepository.findBooksWithScroll(member.getId(), requestDTO);

            // then
            assertThat(result).hasSize(4);

            // createdDate로 정렬되었는지 확인
            assertThat(result)
                .extracting(book -> book.getCreatedDate())
                .isSortedAccordingTo(Comparator.reverseOrder());

            // 또는 id로 정렬되었는지 확인
            assertThat(result)
                .extracting(MyBook::getId)
                .isSortedAccordingTo(Comparator.reverseOrder());
        }

        @Test
        @DisplayName("무한 스크롤 - 두 번째 페이지 조회")
        void findSecondPageTest() {
            // given
            // 1. 첫 페이지 조회
            ScrollRequestDTO firstRequest = ScrollRequestDTO.builder()
                .size(3)
                .sort("desc")
                .status(null)    // status 필터 제거 (null 이면 전체조회)
                .build();

            List<MyBook> firstPage = myBookRepository.findBooksWithScroll(member.getId(),
                firstRequest);
            Long lastId = firstPage.get(2).getId(); // 세 번째 항목의 ID

            // 2. 두 번째 페이지 조회
            ScrollRequestDTO secondRequest = ScrollRequestDTO.builder()
                .lastId(lastId)
                .size(3)
                .sort("desc")
                .status(null)    // status 필터 제거 (null 이면 전체조회)
                .build();

            // when
            List<MyBook> secondPage = myBookRepository.findBooksWithScroll(member.getId(),
                secondRequest);

            // then
            assertThat(secondPage).hasSize(4);
            assertThat(secondPage)
                .extracting("id")
                .allMatch(id -> (Long) id < lastId);
            assertThat(secondPage)
                .extracting("aladinBook.title")
                .containsExactly(
                    "테스트 책 7",
                    "테스트 책 6",
                    "테스트 책 5",
                    "테스트 책 4"
                );
        }

        @Test
        @DisplayName("무한 스크롤 - 마지막 페이지 조회")
        void findLastPageTest() {
            // given
            // 1. 이전 페이지들 조회
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                .size(7)
                .sort("desc")
                .build();

            List<MyBook> previousPage = myBookRepository.findBooksWithScroll(member.getId(),
                request);
            Long lastId = previousPage.get(previousPage.size() - 2).getId();

            // 2. 마지막 페이지 조회
            ScrollRequestDTO lastRequest = ScrollRequestDTO.builder()
                .lastId(lastId)
                .size(3)
                .sort("desc")
                .build();

            // when
            List<MyBook> lastPage = myBookRepository.findBooksWithScroll(member.getId(),
                lastRequest);

            // then
            assertThat(lastPage).hasSizeLessThanOrEqualTo(3);
            assertThat(lastPage)
                .extracting("id")
                .allMatch(id -> (Long) id < lastId);
        }

        @Test
        @DisplayName("상태(Status) 필터링 테스트")
        void findBooksWithStatusTest() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .status("READING")
                .size(10)
                .build();

            // when
            List<MyBook> result = myBookRepository.findBooksWithScroll(member.getId(), requestDTO);

            // then
            assertThat(result)
                .isNotEmpty()
                .extracting("status")
                .containsOnly(BookStatus.READING);
        }

        @Test
        @DisplayName("검색어 필터링 테스트")
        void findWithSearchTest() {
            // given
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                .size(3)
                .sort("desc")
                .search("테스트 책")
                .build();

            // when
            List<MyBook> result = myBookRepository.findBooksWithScroll(member.getId(), request);

            // then
            assertThat(result)
                .isNotEmpty()
                .extracting("aladinBook.title")
                .allMatch(title -> title.toString().contains("테스트 책"));
        }

        @Test
        @DisplayName("잘못된 상태값 입력 시 예외 발생")
        void invalidStatusTest() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .status("INVALID_STATUS")
                .size(10)
                .build();

            // when & then
            assertThatThrownBy(() ->
                myBookRepository.findBooksWithScroll(member.getId(), requestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("유효하지 않은 도서 상태입니다");
        }

        @Test
        @DisplayName("데이터가 없는 경우")
        void findWithNoDataTest() {
            // given
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                .size(3)
                .sort("desc")
                .search("존재하지않는데이터")
                .build();

            // when
            List<MyBook> result = myBookRepository.findBooksWithScroll(member.getId(), request);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("도서 삭제 테스트")
    class DeleteMyBookTest {

        @Test
        @DisplayName("내 책 정상 삭제")
        void deleteMyBookSuccess() {
            // given
            MyBook firstBook = myBookRepository.findBooksWithScroll(
                member.getId(),
                ScrollRequestDTO.builder()
                    .size(1)
                    .sort("asc")
                    .status(null)  // status null로 명시
                    .build()
            ).get(0);

            // when
            myBookRepository.deleteByIdAndMemberId(firstBook.getId(), member.getId());
            em.flush();
            em.clear();

            // then
            List<MyBook> result = myBookRepository.findBooksWithScroll(
                member.getId(),
                ScrollRequestDTO.builder()
                    .size(10)
                    .status(null)  // status null로 명시
                    .build()
            );
            assertThat(result).hasSize(9);  // 10개 중 1개 삭제되어 9개
            assertThat(result).extracting("id")
                .doesNotContain(firstBook.getId());
        }

        @Test
        @DisplayName("다른 사용자의 책 삭제 시도")
        void deleteOtherUserBookFail() {
            // given
            MyBook firstBook = myBookRepository.findBooksWithScroll(
                member.getId(),
                ScrollRequestDTO.builder()
                    .size(1)
                    .sort("asc")
                    .status(null)  // status null로 명시
                    .build()
            ).get(0);

            Long wrongMemberId = member.getId() + 999L;  // 잘못된 member ID

            // when
            myBookRepository.deleteByIdAndMemberId(firstBook.getId(), wrongMemberId);
            em.flush();
            em.clear();

            // then
            List<MyBook> result = myBookRepository.findBooksWithScroll(
                member.getId(),
                ScrollRequestDTO.builder()
                    .size(10)
                    .status(null)  // status null로 명시
                    .build()
            );
            assertThat(result).hasSize(10);  // 삭제되지 않고 10개 그대로
            assertThat(result).extracting("id")
                .contains(firstBook.getId());
        }
    }

    @Nested
    @DisplayName("회원의 책 목록 조회 테스트")
    class FindByMemberIdTest {

        @Test
        @DisplayName("회원이 가진 모든 책 조회 성공")
        void findByMemberIdSuccess() {
            // given
            // setUp()에서 이미 10개의 책이 생성되어 있음

            // when
            Set<MyBook> myBooks = myBookRepository.findByMemberId(member.getId());

            // then
            assertThat(myBooks).hasSize(10);  // 10개의 책이 모두 조회되는지 확인
            assertThat(myBooks)
                .extracting("member.id")
                .containsOnly(member.getId());  // 모든 책이 해당 member의 것인지 확인
        }

        @Test
        @DisplayName("존재하지 않는 회원의 책 목록 조회")
        void findByNonExistentMemberId() {
            // given
            Long nonExistentMemberId = 999999L;

            // when
            Set<MyBook> myBooks = myBookRepository.findByMemberId(nonExistentMemberId);

            // then
            assertThat(myBooks).isEmpty();  // 결과가 빈 Set인지 확인
        }

        @Test
        @DisplayName("조회된 책의 ISBN13 값 확인")
        void checkMyBooksIsbn13() {
            // given
            String expectedFirstIsbn = "9728956740001";  // setUp()에서 생성된 첫 번째 책의 ISBN

            // when
            Set<MyBook> myBooks = myBookRepository.findByMemberId(member.getId());

            // then
            assertThat(myBooks)
                .extracting("isbn13")
                .contains(expectedFirstIsbn);
        }

        @Test
        @DisplayName("조회된 책에 AladinBook이 포함되어 있는지 확인")
        void checkMyBooksContainsAladinBook() {
            // when
            Set<MyBook> myBooks = myBookRepository.findByMemberId(member.getId());

            // then
            assertThat(myBooks)
                .extracting("aladinBook")
                .allMatch(aladinBook -> aladinBook != null);

            // AladinBook의 title이 예상대로 들어있는지 확인
            assertThat(myBooks)
                .extracting("aladinBook.title")
                .contains("테스트 책 1", "테스트 책 2", "테스트 책 3");
        }
    }

    @Test
    @DisplayName("완료된 책을 카테고리별로 그룹화하여 조회")
    void findCompletedBooksGroupedByYears() {

        // when
        List<Object[]> result = myBookRepository.findCompletedBooksGroupedByCategory(
            member.getId());

        // then
        assertThat(result).isNotEmpty();  // 결과가 비어 있지 않음을 확인

        // 결과가 카테고리별로 그룹화된 책 수를 확인
        assertThat(result).anyMatch(record -> {
            String categoryName = (String) record[0];
            Long count = (Long) record[1];
            // 카테고리별 책의 수가 1개 이상임을 확인
            return count >= 1;
        });

        // 예시로 카테고리명이 "IT/컴퓨터"일 때 책 수 확인 (categoryName과 count를 출력하여 디버깅 가능)
        result.forEach(record -> {
            String categoryName = (String) record[0];
            Long count = (Long) record[1];
            System.out.println("Category: " + categoryName + ", Count: " + count);
        });
    }

    @Test
    @DisplayName("완료된 책을 연도별로 그룹화하여 조회")
    void findCompletedBooksGroupedByYear() {
        // when
        List<Object[]> result = myBookRepository.findCompletedBooksGroupedByYear(member.getId());

        // then
        assertThat(result).isNotEmpty();  // 결과가 비어 있지 않음을 확인

        // 결과 출력
        result.forEach(row -> {
            System.out.println("Year: " + row[0] + ", Count: " + row[1]);
            assertThat(row[0]).isInstanceOf(Integer.class);  // 연도는 Integer이어야 함
            assertThat(row[1]).isInstanceOf(Long.class);     // 카운트 값은 Long이어야 함
        });
    }

    @Test
    @DisplayName("완료된 책을 연_월별로 그룹화하여 조회")
    void findMonthCompletedBooksByYear() {
        // when
        List<Object[]> result = myBookRepository.findCompletedBooksGroupedByYearAndMonth(member.getId());

        // then
        assertThat(result).isNotEmpty();  // 결과가 비어 있지 않음을 확인

        // 결과 출력
        result.forEach(row -> {
            System.out.println("Year-Month: " + row[0] + ", Count: " + row[1]);
            assertThat(row[0]).isInstanceOf(String.class);  // 연_월은 String이어야 함 (YYYY-MM 형식)
            assertThat(row[1]).isInstanceOf(Long.class);    // 카운트 값은 Long이어야 함
        });
    }

    @Nested
    @DisplayName("연도별 월별 완료된 책 수 조회 테스트")
    class FindMonthCompletedBooksByYearTest {

        @Test
        @DisplayName("연도별 월별 완료된 책 수 조회")
        void findMonthCompletedBooksByYearSuccess() {

            // 연도별 완료된 책 데이터 추가
            LocalDate currentDate = LocalDate.now();
            for (int i = 0; i < 3; i++) {
                AladinBook book = AladinBook.builder()
                    .isbn13(String.format("978895674%04d", 11 + i))
                    .title("연도별 테스트 책 " + i)
                    .author("연도별 작가 " + i)
                    .link("http://example.com/book/" + (11 + i))
                    .cover("http://example.com/cover/" + (11 + i))
                    .fullDescription("연도별 테스트 책 " + i + "의 상세 설명입니다. 이 책은 테스트를 위해 생성된 가상의 책입니다.")
                    .fullDescription2("연도별 테스트 책 " + i + "의 출판사 제공 추가 설명입니다. 이 책은 테스트 데이터로 사용됩니다.")
                    .publisher("테스트출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(4.0)
                    .itemPage(300)
                    .build();
                em.persist(book);

                MyBook myBook = MyBook.builder()
                    .member(member)
                    .aladinBook(book)
                    .isbn13(book.getIsbn13())
                    .status(BookStatus.COMPLETED)
                    .myRating(4.5)
                    .oneLineReview("연도별 한줄평 " + i)
                    .currentPage(300)
                    .startDate(currentDate.minusYears(i).minusMonths(1))
                    .endDate(currentDate.minusYears(i))
                    .build();
                em.persist(myBook);
            }

            // given
            Integer year = 2023;  // 테스트할 연도
            Long memberId = member.getId();  // 회원 ID

            // 특정 월에 완료된 책을 만들어 놓은 상태
            for (int i = 1; i <= 10; i++) {
                MyBook myBook = MyBook.builder()
                    .member(member)
                    .aladinBook(aladinBookRepository.findAll().get(i % 10))  // 임의로 책을 할당
                    .isbn13("978895674" + String.format("%04d", i))
                    .status(BookStatus.COMPLETED)  // 상태를 'COMPLETED'로 설정
                    .endDate(LocalDate.of(year, (i % 12) + 1, i % 28 + 1))  // 월별로 완료일 설정
                    .build();
                em.persist(myBook);
            }

            em.flush();
            em.clear();

            // when
            List<MyBook> result = myBookRepository.findCompletedBooksByYear(year, memberId);

            // then
            assertThat(result).isNotEmpty();  // 결과가 비어 있지 않음
            assertThat(result).hasSize(11);  // 위에서 넣은 10개 + setUp에서 넣은 책 1권 의 책데이터 만큼

            System.out.println("result = " + result);
        }

        @Test
        @DisplayName("연도별 완료된 책이 없는 경우")
        void findMonthCompletedBooksByYearNoData() {
            // given
            Integer year = 2021;  // 완료된 책이 없는 연도
            Long memberId = member.getId();

            // when
            List<MyBook> result = myBookRepository.findCompletedBooksByYear(year, memberId);

            // then
            assertThat(result).isEmpty();  // 결과가 비어 있어야 함
        }
    }

    @Test
    @DisplayName("연도별 완료된 책의 총 페이지 수 조회")
    void findCompletedBookPageGroupedByYearTest() {
        // given
        LocalDate currentDate = LocalDate.now();
        Long memberId = member.getId();

        // 테스트 데이터 생성
        for (int i = 0; i < 5; i++) {
            MyBook myBook = MyBook.builder()
                .member(member)
                .aladinBook(aladinBookRepository.findAll().get(i % 10))
                .isbn13("978895674" + String.format("%04d", i))
                .status(BookStatus.COMPLETED)
                .currentPage((i + 1) * 100)
                .endDate(currentDate.minusYears(i % 3))
                .build();
            em.persist(myBook);
        }
        em.flush();
        em.clear();

        // when
        List<Object[]> result = myBookRepository.findCompletedBookPageGroupedByYear(memberId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isLessThanOrEqualTo(3); // 최대 3년치 데이터

        for (Object[] row : result) {
            assertThat(row[0]).isInstanceOf(Integer.class); // 연도는 Integer
            assertThat(row[1]).isInstanceOf(Long.class);    // 총 페이지 수는 Long

            Integer year = (Integer) row[0];
            Long totalPages = (Long) row[1];

            assertThat(year).isGreaterThanOrEqualTo(currentDate.getYear() - 2);
            assertThat(totalPages).isPositive();

            System.out.println("Year: " + year + ", Total Pages: " + totalPages);
        }

        // 결과가 연도 내림차순으로 정렬되었는지 확인
        assertThat(result)
            .extracting(row -> (Integer) row[0])
            .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("완료된 책을 연도별, 카테고리별로 그룹화하여 조회")
    void findCompletedBooksGroupedByYearsTest() {
        // when
        List<Object[]> result = myBookRepository.findCompletedBooksGroupedByYears(member.getId());

        // then
        assertThat(result).isNotEmpty();

        // 결과가 연도별, 카테고리별로 그룹화된 책 수를 확인
        assertThat(result).allMatch(record -> {
            String categoryName = (String) record[0];
            Integer year = (Integer) record[1];
            Long bookCount = (Long) record[2];

            // 카테고리명, 연도, 책 수가 모두 null이 아님을 확인
            return categoryName != null && year != null && bookCount != null;
        });

        // 연도가 현재 연도부터 2년 전까지인지 확인
        int currentYear = LocalDate.now().getYear();
        assertThat(result).allMatch(record -> {
            Integer year = (Integer) record[1];
            return year >= currentYear - 2 && year <= currentYear;
        });

        // 결과가 연도의 내림차순으로 정렬되어 있는지 확인
        assertThat(result)
            .extracting(record -> (Integer) record[1])
            .isSortedAccordingTo(Comparator.reverseOrder());

        // 디버깅을 위해 결과 출력
        result.forEach(record -> {
            String categoryName = (String) record[0];
            Integer year = (Integer) record[1];
            Long bookCount = (Long) record[2];
            System.out.println("Category: " + categoryName + ", Year: " + year + ", Count: " + bookCount);
        });
    }

}