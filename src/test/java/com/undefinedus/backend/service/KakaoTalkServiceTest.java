package com.undefinedus.backend.service;


import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import com.undefinedus.backend.scheduler.KakaoTalkSender;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@DBRider
@ActiveProfiles("test")
class KakaoTalkServiceTest {

    @Autowired
    private KakaoTalkService kakaoTalkService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MyBookmarkRepository myBookmarkRepository;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @MockBean
    private KakaoTalkSender kakaoTalkSender;

    @BeforeEach
    public void setup() {
        // 테스트 전 데이터베이스 초기화
        memberRepository.deleteAll();
        myBookmarkRepository.deleteAll();

// Member 데이터 생성
        Member member1 = Member.builder()
            .username("user1@example.com")
            .password("password123")
            .nickname("User1")
            .isPublic(true)
            .kakaoRefreshToken("refresh_token_1")
            .isMessageToKakao(true)
            .memberRoleList(List.of(MemberType.USER))
            .preferences(new HashSet<>(Arrays.asList(PreferencesType.가정_요리_뷰티, PreferencesType.만화)))
            .build();

        Member member2 = Member.builder()
            .username("user2@example.com")
            .password("password456")
            .nickname("User2")
            .isPublic(true)
            .kakaoRefreshToken("refresh_token_2")
            .isMessageToKakao(true)
            .memberRoleList(List.of(MemberType.USER))
            .preferences(new HashSet<>(Arrays.asList(PreferencesType.과학, PreferencesType.건강_취미_레저)))
            .build();

        memberRepository.saveAll(Arrays.asList(member1, member2));

// AladinBook 데이터 생성
        AladinBook book1 = AladinBook.builder()
            .isbn13("9788956740001")
            .title("테스트 책 1")
            .author("작가 1")
            .link("http://example.com/book1")
            .cover("http://example.com/cover1")
            .fullDescription("테스트 책 1의 상세 설명입니다.")
            .fullDescription2("출판사에서 제공한 테스트 책 1의 추가 설명입니다.")
            .publisher("출판사 1")
            .categoryName("소설")
            .customerReviewRank(4.5)
            .itemPage(300)
            .build();

        AladinBook book2 = AladinBook.builder()
            .isbn13("9788956740002")
            .title("테스트 책 2")
            .author("작가 2")
            .link("http://example.com/book2")
            .cover("http://example.com/cover2")
            .fullDescription("테스트 책 2의 상세 설명입니다.")
            .fullDescription2("출판사에서 제공한 테스트 책 2의 추가 설명입니다.")
            .publisher("출판사 2")
            .categoryName("과학")
            .customerReviewRank(4.0)
            .itemPage(250)
            .build();

        aladinBookRepository.saveAll(Arrays.asList(book1, book2));

// MyBookmark 데이터 생성
        MyBookmark bookmark1 = MyBookmark.builder()
            .aladinBook(book1)
            .member(member1)
            .phrase("첫 번째 북마크 문구")
            .pageNumber(50)
            .build();

        MyBookmark bookmark2 = MyBookmark.builder()
            .aladinBook(book1)
            .member(member1)
            .phrase("두 번째 북마크 문구")
            .pageNumber(100)
            .build();

        MyBookmark bookmark3 = MyBookmark.builder()
            .aladinBook(book2)
            .member(member2)
            .phrase("세 번째 북마크 문구")
            .pageNumber(75)
            .build();

        MyBookmark bookmark4 = MyBookmark.builder()
            .aladinBook(book2)
            .member(member2)
            .phrase("네 번째 북마크 문구")
            .pageNumber(150)
            .build();

        myBookmarkRepository.saveAll(Arrays.asList(bookmark1, bookmark2, bookmark3, bookmark4));

// MyBook 데이터 생성
        MyBook myBook1 = MyBook.builder()
            .member(member1)
            .aladinBook(book1)
            .isbn13("9788956740001")
            .status(BookStatus.COMPLETED)
            .myRating(4.5)
            .oneLineReview("좋은 책이었습니다")
            .currentPage(300)
            .updateCount(1)
            .startDate(LocalDate.of(2023, 1, 1))
            .endDate(LocalDate.of(2023, 1, 15))
            .build();

        MyBook myBook2 = MyBook.builder()
            .member(member2)
            .aladinBook(book2)
            .isbn13("9788956740002")
            .status(BookStatus.READING)
            .currentPage(150)
            .updateCount(1)
            .startDate(LocalDate.of(2023, 2, 1))
            .build();

        myBookRepository.saveAll(Arrays.asList(myBook1, myBook2));
    }

    @Test
    public void testSendKakaoTalk() {
        // 테스트 실행
        kakaoTalkService.sendKakaoTalk();

    }
}