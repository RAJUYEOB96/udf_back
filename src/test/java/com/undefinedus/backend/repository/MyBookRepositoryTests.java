package com.undefinedus.backend.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.BookStatus;
import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
public class MyBookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Autowired
    private CalendarStampRepository calendarStampRepository;

    @Test
    @DisplayName("BookRepository 연결 확인 테스트")
    void checkConnections() {
        assertNotNull(bookRepository, "BookRepository가 주입되지 않았습니다.");
    }

    @Test
    @DisplayName("테스트용 북 10개 insert")
    @Commit
    void insert10Book() {

        Long memberId = 1L; // DB 보고 있는거 찾아서

        Optional<Member> findMember = memberRepository.findById(memberId);

        if (findMember.isEmpty()) {
            System.out.println("해당 member가 존재하지 않습니다 : " + memberId);
            return;
        }

        for (int i = 0; i < 10; i++) {

            String isbn13 = String.valueOf(i); // DB 보고 있는거 찾아서

            Optional<AladinBook> findAladinBook = aladinBookRepository.findByIsbn13(isbn13);

            if (findAladinBook.isEmpty()) {
                System.out.println("해당 aladinBook이 존재하지 않습니다 : " + isbn13);
                return;
            }

            MyBook myBook = MyBook.builder()
                .member(findMember.get())
                .aladinBook(findAladinBook.get())
                .isbn13(findAladinBook.get().getIsbn13())
                .status(BookStatus.READING)
                .myRating(5.0)
                .oneLineReview("test")
                .currentPage(100)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now())
                .build();

            CalendarStamp calendarStamp = CalendarStamp.builder()
                .member(findMember.get())
                .myBook(myBook)
                .recordDate(LocalDate.now())
                .status(myBook.getStatus())
                .build();

            bookRepository.save(myBook);
        }

    }

    @Test
    @DisplayName("findByMemberIdAndIsbn13 메서드 테스트")
    void testFindByMemberIdAndIsbn13(){
        // given
        String isbn13 = "1";
        Long memberId = 1L;

        // when
        Optional<MyBook> findBook = bookRepository.findByMemberIdAndIsbn13(memberId,
            isbn13);

        if (findBook.isEmpty()) {
            System.out.println("해당 book이 존재하지 않습니다.");
            return;
        }

        // then
        System.out.println("findBook : " + findBook.get());
    }

    @Test
    @DisplayName("findByMemberIdAndIsbn13 메서드 테스트 - 실패할 경우")
    void testFindByMemberIdAndIsbn13WhenFail(){
        // given
        String isbn13 = "11";
        Long memberId = 1L;

        // when
        Optional<MyBook> findBook = bookRepository.findByMemberIdAndIsbn13(memberId,
            isbn13);

        if (findBook.isEmpty()) {
            System.out.println("해당 book이 존재하지 않습니다.");
            return;
        }

        // then
        System.out.println("findBook : " + findBook.get());
    }


}