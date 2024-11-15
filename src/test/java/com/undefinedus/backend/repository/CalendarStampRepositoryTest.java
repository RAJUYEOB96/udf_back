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
        AladinBook aladinBook = AladinBook.builder()
                .isbn13("9788956746425")
                .title("테스트 책")
                .author("테스트 작가")
                .itemPage(300)
                .cover("test-cover-url")
                .link("test-link")
                .fullDescription("테스트 설명")
                .publisher("테스트 출판사")
                .categoryName("IT/컴퓨터")
                .customerReviewRank(4.5)
                .build();
        em.persist(aladinBook);
        
        // 테스트 MyBook 생성
        testMyBook = MyBook.builder()
                .member(testMember)
                .aladinBook(aladinBook)
                .isbn13(aladinBook.getIsbn13())
                .status(BookStatus.READING)
                .build();
        myBookRepository.save(testMyBook);
        
        // 테스트 CalendarStamp 생성
        testStamp = CalendarStamp.builder()
                .member(testMember)
                .myBook(testMyBook)
                .bookCoverUrl(aladinBook.getCover())
                .recordedAt(LocalDate.now())
                .status(BookStatus.READING)
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
        Integer count = calendarStampRepository.countByMemberIdAndMyBookId(nonExistentId, nonExistentId);
        
        // then
        assertEquals(0, count); // 존재하지 않는 데이터는 0 반환
    }
  
}