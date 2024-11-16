package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.request.BookScrollRequestDTO;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// @SpringBootTest: 스프링 부트 애플리케이션 컨텍스트를 로드하여 통합 테스트를 수행합니다.
// @Transactional: 각 테스트 메서드가 끝나면 데이터베이스를 롤백하여 테스트 격리를 보장합니다.
@SpringBootTest
@Transactional
class MyBookRepositoryTest {
    
    @Autowired
    private MyBookRepository myBookRepository;
    
    @Autowired
    private EntityManager em;
    
    private Member member;
    
    /**
     * 각 테스트 전에 실행되어 테스트 데이터를 준비합니다.
     */
    @BeforeEach
    void setUp() {
        // === Member 생성 === //
        member = Member.builder()
                .username("test@test.com")
                .password("password123")
                .nickname("tester")
                .memberRoleList(List.of(MemberType.USER))
                .isPublic(true)
                .build();
        em.persist(member);
        
        // === 10개의 테스트용 책 데이터 생성 === //
        for (int i = 1; i <= 10; i++) {
            AladinBook book = AladinBook.builder()
                    .isbn13(String.format("978895674%04d", i))
                    .title("테스트 책 " + i)
                    .author(i % 2 == 0 ? "김작가" : "이작가")
                    .link("http://example.com/" + i)
                    .cover("http://example.com/cover" + i)
                    .fullDescription("테스트 설명 " + i)
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
            BookScrollRequestDTO requestDTO = BookScrollRequestDTO.builder()
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
            BookScrollRequestDTO firstRequest = BookScrollRequestDTO.builder()
                    .size(3)
                    .sort("desc")
                    .status(null)    // status 필터 제거 (null 이면 전체조회)
                    .build();
            
            List<MyBook> firstPage = myBookRepository.findBooksWithScroll(member.getId(), firstRequest);
            Long lastId = firstPage.get(2).getId(); // 세 번째 항목의 ID
            
            // 2. 두 번째 페이지 조회
            BookScrollRequestDTO secondRequest = BookScrollRequestDTO.builder()
                    .lastId(lastId)
                    .size(3)
                    .sort("desc")
                    .status(null)    // status 필터 제거 (null 이면 전체조회)
                    .build();
            
            // when
            List<MyBook> secondPage = myBookRepository.findBooksWithScroll(member.getId(), secondRequest);
            
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
            BookScrollRequestDTO request = BookScrollRequestDTO.builder()
                    .size(7)
                    .sort("desc")
                    .build();
            
            List<MyBook> previousPage = myBookRepository.findBooksWithScroll(member.getId(), request);
            Long lastId = previousPage.get(previousPage.size() - 2).getId();
            
            // 2. 마지막 페이지 조회
            BookScrollRequestDTO lastRequest = BookScrollRequestDTO.builder()
                    .lastId(lastId)
                    .size(3)
                    .sort("desc")
                    .build();
            
            // when
            List<MyBook> lastPage = myBookRepository.findBooksWithScroll(member.getId(), lastRequest);
            
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
            BookScrollRequestDTO requestDTO = BookScrollRequestDTO.builder()
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
            BookScrollRequestDTO request = BookScrollRequestDTO.builder()
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
            BookScrollRequestDTO requestDTO = BookScrollRequestDTO.builder()
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
            BookScrollRequestDTO request = BookScrollRequestDTO.builder()
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
    @DisplayName("도서 Lazy Loading 테스트")
    class BookLazyLoadingTest {
        
        @Test
        @DisplayName("기본 조회시 AladinBook은 프록시로 조회됨")
        void lazyLoadingTest() {
            // given
            MyBook myBook = myBookRepository.findById(1L).orElseThrow();
            
            // when & then
            assertThat(myBook.getAladinBook()).isInstanceOf(HibernateProxy.class);
        }
        
        @Test
        @DisplayName("fetch join 사용시 AladinBook이 즉시 로딩됨")
        void fetchJoinTest() {
            
            Long bookId = 1L;       // 실제 initData로 만들어서 들어있는 bookId
            Long memberId = 2L;     // 실제 initData로 만들어서 bookId가 가지고 있는 memberId // 다를시 null이 나와야함
            
            // given
            MyBook myBook = myBookRepository.findByIdAndMemberIdWithAladinBook(bookId, memberId).orElseThrow();
            
            // when & then
            assertThat(myBook.getAladinBook()).isNotInstanceOf(HibernateProxy.class);
            assertThat(myBook.getAladinBook()).isInstanceOf(AladinBook.class);
            
            assertThat(myBook.getAladinBook())
                    .extracting(    // 필드들 이름이 있는지 추출
                            "isbn13",
                            "title",
                            "author",
                            "link",
                            "cover",
                            "publisher",
                            "categoryName"
                    )
                    .containsExactly(   // 안의 내용이 있는지 확인 (DB에 실제 저장된 것들) 정확히 일치 필요 // 필요시 변경
                            "9791165341909",  // isbn13
                            "달러구트 꿈 백화점",      // title
                            "이미예",         // author (1번째는 이작가)
                            "https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=247270655", // link
                            "https://image.aladin.co.kr/product/24727/6/cover/k582730586_1.jpg", // cover
                            "팩토리나인",    // publisher
                            "국내도서>소설"       // categoryName
                    );
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
                    BookScrollRequestDTO.builder()
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
                    BookScrollRequestDTO.builder()
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
                    BookScrollRequestDTO.builder()
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
                    BookScrollRequestDTO.builder()
                            .size(10)
                            .status(null)  // status null로 명시
                            .build()
            );
            assertThat(result).hasSize(10);  // 삭제되지 않고 10개 그대로
            assertThat(result).extracting("id")
                    .contains(firstBook.getId());
        }
    }
}