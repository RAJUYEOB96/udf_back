package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Log4j2
@Transactional
class CalendarStampRepositoryTest {

    @Autowired
    private CalendarStampRepository calendarStampRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private EntityManager em;

    private Member testMember;
    private MyBook testMyBook;
    private AladinBook testAladinBook;
    private CalendarStamp testStamp;

    @BeforeEach
    void setUp() {
        // 테스트 Member 생성
        testMember = Member.builder()
            .username("test@test.com")
            .password("password")
            .nickname("tester")
            .memberRoleList(List.of(MemberType.USER))
            .isPublic(true)
            .build();
        memberRepository.save(testMember);

        // 테스트 AladinBook 생성
        testAladinBook = AladinBook.builder()
            .isbn13("9788956746425")
            .title("테스트 책")
            .author("테스트 작가")
            .itemPage(300)
            .cover("test-cover-url")
            .link("test-link")
            .fullDescription("테스트 설명")
            .fullDescription2("test")
            .publisher("테스트 출판사")
            .categoryName("IT/컴퓨터")
            .customerReviewRank(4.5)
            .build();
        em.persist(testAladinBook);

        // 테스트 MyBook 생성
        testMyBook = MyBook.builder()
            .member(testMember)
            .aladinBook(testAladinBook)
            .isbn13(testAladinBook.getIsbn13())
            .status(BookStatus.READING)
            .build();
        myBookRepository.save(testMyBook);

        // 테스트 CalendarStamp 생성
        testStamp = CalendarStamp.builder()
            .member(testMember)
            .myBookId(testMyBook.getId())
            .bookTitle(testAladinBook.getTitle())
            .bookAuthor(testAladinBook.getAuthor())
            .bookCover(testAladinBook.getCover())
            .recordedAt(LocalDate.now())
            .status(BookStatus.READING)
            .itemPage(testAladinBook.getItemPage())
            .currentPage(testMyBook.getCurrentPage())
            .startDate(LocalDate.now().minusDays(7))
            .endDate(LocalDate.now())
            .readDateCount(1)
            .build();
        calendarStampRepository.save(testStamp);

        // 영속성 컨텍스트 초기화
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("연결 확인")
    void testConnection() {
        assertNotNull(calendarStampRepository);
    }

    @Test
    @DisplayName("countByMemberIdAndBookId 메서드 테스트")
    void testCountByMemberIdAndBookId() {
        // ID가 자동으로 생성되는 이유:
        // @Id 필드에 @GeneratedValue(strategy = GenerationType.IDENTITY) 설정
        // 엔티티 저장(save/persist) 시 DB에서 ID 자동 생성
        // 생성된 ID는 엔티티 객체에 자동으로 set됨

        // given - setUp에서 생성된 데이터 사용
        Long memberId = testMember.getId();
        Long bookId = testMyBook.getId();

        // when
        Integer count = calendarStampRepository.countByMemberIdAndMyBookId(memberId, bookId);

        // then
        assertEquals(1, count); // 생성된 스탬프 1개 확인

        log.info("Member ID: {}", memberId);
        log.info("Book ID: {}", bookId);
        log.info("Count: {}", count);
    }

    @Test
    @DisplayName("존재하지 않는 데이터 카운트 테스트")
    void testCountNonExistentData() {
        // given
        Long nonExistentId = 999L;

        // when
        Integer count = calendarStampRepository.countByMemberIdAndMyBookId(nonExistentId,
            nonExistentId);

        // then
        assertEquals(0, count); // 존재하지 않는 데이터는 0 반환
    }

    @Test
    @DisplayName("MyBook ID로 CalendarStamp 삭제 테스트")
    void testDeleteAllByMyBookId() {
        // given
        Long myBookId = testMyBook.getId();

        // 삭제 전 개수 확인
        Integer beforeCount = calendarStampRepository.countByMemberIdAndMyBookId(
            testMember.getId(), myBookId);
        assertEquals(1, beforeCount); // setUp에서 생성한 1개 존재 확인

        // when
        calendarStampRepository.deleteAllByMyBookId(myBookId);
        em.flush();  // 변경사항을 DB에 반영
        em.clear();  // 영속성 컨텍스트 초기화

        // then
        Integer afterCount = calendarStampRepository.countByMemberIdAndMyBookId(
            testMember.getId(), myBookId);
        assertEquals(0, afterCount); // 삭제 후 0개 확인
    }

    @Test
    @DisplayName("여러 개의 CalendarStamp 삭제 테스트")
    void testDeleteMultipleStamps() {
        // given - 추가 스탬프 생성
        CalendarStamp additionalStamp = CalendarStamp.builder()
            .member(testMember)
            .myBookId(testMyBook.getId())
            .bookTitle(testAladinBook.getTitle())
            .bookAuthor(testAladinBook.getAuthor())
            .bookCover(testAladinBook.getCover())
            .recordedAt(LocalDate.now())
            .status(BookStatus.READING)
            .itemPage(testAladinBook.getItemPage())
            .currentPage(testMyBook.getCurrentPage())
            .startDate(LocalDate.now().minusDays(7))
            .endDate(LocalDate.now())
            .readDateCount(1)
            .build();
        calendarStampRepository.save(additionalStamp);
        em.flush();
        em.clear();

        Long myBookId = testMyBook.getId();

        // 삭제 전 개수 확인
        Integer beforeCount = calendarStampRepository.countByMemberIdAndMyBookId(
            testMember.getId(), myBookId);
        assertEquals(2, beforeCount); // 총 2개 존재 확인

        // when
        calendarStampRepository.deleteAllByMyBookId(myBookId);
        em.flush();
        em.clear();

        // then
        Integer afterCount = calendarStampRepository.countByMemberIdAndMyBookId(
            testMember.getId(), myBookId);
        assertEquals(0, afterCount); // 모두 삭제되어 0개
    }
    
    @Test
    @DisplayName("연도와 월에 해당하는 CalendarStamp 조회 테스트")
    void testGetAllStampsWhenYearAndMonth() {
        // given
        LocalDate date1 = LocalDate.of(2023, 5, 1);
        LocalDate date2 = LocalDate.of(2023, 5, 15);
        LocalDate date3 = LocalDate.of(2023, 6, 1);
        
        // 다른 회원 생성 및 CalendarStamp 데이터 추가
        Member otherMember = Member.builder()
                .username("other@test.com")
                .password("password")
                .nickname("otherTest")
                .memberRoleList(List.of(MemberType.USER))
                .isPublic(true)
                .build();
        memberRepository.save(otherMember);
        
        CalendarStamp otherStamp = CalendarStamp.builder()
                .member(otherMember)
                .myBookId(testMyBook.getId())
                .bookTitle(testAladinBook.getTitle())
                .bookAuthor(testAladinBook.getAuthor())
                .bookCover(testAladinBook.getCover())
                .recordedAt(date1)
                .status(BookStatus.READING)
                .itemPage(testAladinBook.getItemPage())
                .currentPage(testMyBook.getCurrentPage())
                .startDate(date1.minusDays(7))
                .endDate(date1)
                .readDateCount(1)
                .build();
        calendarStampRepository.save(otherStamp);
        
        CalendarStamp stamp1 = CalendarStamp.builder()
                .member(testMember)
                .myBookId(testMyBook.getId())
                .bookTitle(testAladinBook.getTitle())
                .bookAuthor(testAladinBook.getAuthor())
                .bookCover(testAladinBook.getCover())
                .recordedAt(date1)
                .status(BookStatus.READING)
                .itemPage(testAladinBook.getItemPage())
                .currentPage(testMyBook.getCurrentPage())
                .startDate(date1.minusDays(7))
                .endDate(date1)
                .readDateCount(1)
                .build();
        
        CalendarStamp stamp2 = CalendarStamp.builder()
                .member(testMember)
                .myBookId(testMyBook.getId())
                .bookTitle(testAladinBook.getTitle())
                .bookAuthor(testAladinBook.getAuthor())
                .bookCover(testAladinBook.getCover())
                .recordedAt(date2)
                .status(BookStatus.READING)
                .itemPage(testAladinBook.getItemPage())
                .currentPage(testMyBook.getCurrentPage())
                .startDate(date2.minusDays(7))
                .endDate(date2)
                .readDateCount(1)
                .build();
        
        CalendarStamp stamp3 = CalendarStamp.builder()
                .member(testMember)
                .myBookId(testMyBook.getId())
                .bookTitle(testAladinBook.getTitle())
                .bookAuthor(testAladinBook.getAuthor())
                .bookCover(testAladinBook.getCover())
                .recordedAt(date3)
                .status(BookStatus.READING)
                .itemPage(testAladinBook.getItemPage())
                .currentPage(testMyBook.getCurrentPage())
                .startDate(date3.minusDays(7))
                .endDate(date3)
                .readDateCount(1)
                .build();
        
        calendarStampRepository.save(stamp1);
        calendarStampRepository.save(stamp2);
        calendarStampRepository.save(stamp3);
        em.flush();
        em.clear();
        
        // when
        List<CalendarStamp> stampsInMay = calendarStampRepository.getAllStampsWhenYearAndMonth(testMember.getId(), 2023, 5);
        log.info("Stamps in May: {}", stampsInMay);
        List<CalendarStamp> stampsInJune = calendarStampRepository.getAllStampsWhenYearAndMonth(testMember.getId(), 2023, 6);
        log.info("Stamps in June: {}", stampsInJune);
        
        // then
        assertEquals(2, stampsInMay.size());
        assertEquals(1, stampsInJune.size());
        assertTrue(stampsInMay.stream().anyMatch(stamp -> stamp.getId().equals(stamp1.getId())));
        assertTrue(stampsInMay.stream().anyMatch(stamp -> stamp.getId().equals(stamp2.getId())));
        assertTrue(stampsInJune.stream().anyMatch(stamp -> stamp.getId().equals(stamp3.getId())));
    }
}