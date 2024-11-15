package com.undefinedus.backend.config;


import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Log4j2
@RequiredArgsConstructor
@Transactional
public class InitialDataConfig {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AladinBookRepository aladinBookRepository;
    private final MyBookRepository myBookRepository;

    @PostConstruct
    public void initData() {
        if (memberRepository.count() > 0) {
            log.info("Data already exists. Skipping initialization.");
            return;
        }
        settingMembers();
        settingAladinBooks();
        settingMyBooks();
    }

    @Transactional
    public void settingMembers() {
        // 이미 관리자가 존재하는지 확인
        if (memberRepository.findByUsername("admin@book.com").isPresent()) {
            log.info("Test member data already exists. Skipping member initialization.");
            return;
        }

        List<Member> members = new ArrayList<>();

        // 관리자
        Member admin = Member.builder()
            .username("admin@book.com")
            .password(passwordEncoder.encode("admin1234"))
            .nickname("관리자")
            .profileImage("https://example.com/admin.jpg")
            .introduction("서비스 관리자입니다.")
            .birth(LocalDate.of(1990, 1, 1))
            .gender("MALE")
            .memberRoleList(List.of(MemberType.ADMIN, MemberType.USER))
            .preferences(
                Set.of(PreferencesType.소설_시_희곡, PreferencesType.과학, PreferencesType.컴퓨터_모바일))
            .isPublic(true)
            .build();
        members.add(admin);

        // 일반 사용자 1
        Member user1 = Member.builder()
            .username("reader1@gmail.com")
            .password(passwordEncoder.encode("user1234"))
            .nickname("책벌레")
            .profileImage("https://example.com/user1.jpg")
            .introduction("소설과 시를 좋아합니다.")
            .birth(LocalDate.of(1995, 3, 15))
            .gender("FEMALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(
                Set.of(PreferencesType.소설_시_희곡, PreferencesType.예술_대중문화, PreferencesType.에세이))
            .isPublic(true)
            .build();
        members.add(user1);

        // 카카오 소셜 로그인 사용자
        Member socialUser = Member.builder()
            .username("12345678")
            .password(passwordEncoder.encode("social1234"))
            .nickname("카카오독서러")
            .profileImage("https://example.com/social.jpg")
            .introduction("카카오로 로그인하는 독서광입니다.")
            .birth(LocalDate.of(1998, 7, 20))
            .gender("MALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(
                Set.of(PreferencesType.컴퓨터_모바일, PreferencesType.경제경영, PreferencesType.자기계발))
            .isPublic(true)
            .isMessageToKakao(true)
            .build();

        SocialLogin kakaoLogin = SocialLogin.builder()
            .member(socialUser)
            .provider("KAKAO")
            .providerId("12345678")
            .build();
        socialUser.setSocialLogin(kakaoLogin);
        members.add(socialUser);

        // 일반 사용자 2
        Member user2 = Member.builder()
            .username("bookworm@naver.com")
            .password(passwordEncoder.encode("book1234"))
            .nickname("독서광")
            .introduction("인문학과 역사 책을 좋아합니다.")
            .birth(LocalDate.of(1992, 5, 10))
            .gender("MALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(Set.of(PreferencesType.인문학, PreferencesType.역사, PreferencesType.사회과학))
            .isPublic(false)
            .build();
        members.add(user2);

        // 일반 사용자 3
        Member user3 = Member.builder()
            .username("novel@daum.net")
            .password(passwordEncoder.encode("novel1234"))
            .nickname("소설가꿈나무")
            .introduction("소설 쓰는 걸 좋아합니다.")
            .birth(LocalDate.of(1997, 9, 25))
            .gender("FEMALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(Set.of(PreferencesType.소설_시_희곡, PreferencesType.에세이, PreferencesType.인문학))
            .isPublic(true)
            .build();
        members.add(user3);

        // 일반 사용자 4
        Member user4 = Member.builder()
            .username("science@gmail.com")
            .password(passwordEncoder.encode("science1234"))
            .nickname("과학덕후")
            .introduction("과학 서적을 즐겨 읽습니다.")
            .birth(LocalDate.of(1993, 11, 30))
            .gender("MALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(Set.of(PreferencesType.과학, PreferencesType.컴퓨터_모바일, PreferencesType.자기계발))
            .isPublic(true)
            .build();
        members.add(user4);

        // 일반 사용자 5
        Member user5 = Member.builder()
            .username("life@naver.com")
            .password(passwordEncoder.encode("life1234"))
            .nickname("라이프스타일러")
            .introduction("요리와 인테리어에 관심이 많아요.")
            .birth(LocalDate.of(1991, 8, 15))
            .gender("FEMALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(
                Set.of(PreferencesType.가정_요리_뷰티, PreferencesType.컴퓨터_모바일, PreferencesType.건강_취미_레저))
            .isPublic(true)
            .build();
        members.add(user5);

        // 일반 사용자 6
        Member user6 = Member.builder()
            .username("health@gmail.com")
            .password(passwordEncoder.encode("health1234"))
            .nickname("건강지킴이")
            .introduction("건강과 운동을 사랑하는 독자입니다.")
            .birth(LocalDate.of(1996, 4, 5))
            .gender("FEMALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(
                Set.of(PreferencesType.자기계발, PreferencesType.건강_취미_레저, PreferencesType.가정_요리_뷰티))
            .isPublic(false)
            .build();
        members.add(user6);

        // 일반 사용자 7
        Member user7 = Member.builder()
            .username("business@daum.net")
            .password(passwordEncoder.encode("business1234"))
            .nickname("경영인")
            .introduction("비즈니스와 자기계발 서적을 주로 읽습니다.")
            .birth(LocalDate.of(1988, 12, 20))
            .gender("MALE")
            .memberRoleList(List.of(MemberType.USER))
            .preferences(Set.of(PreferencesType.경제경영, PreferencesType.자기계발, PreferencesType.인문학))
            .isPublic(true)
            .build();
        members.add(user7);

        // 전체 회원 저장
        memberRepository.saveAll(members);
        log.info("Saved {} test members", members.size());

        // 팔로우 관계 생성
        createFollowRelationship(user1, user2);
        createFollowRelationship(user1, user3);
        createFollowRelationship(user2, user1);
        createFollowRelationship(user3, user1);
        createFollowRelationship(user4, user1);
        createFollowRelationship(user5, user2);
        createFollowRelationship(user6, user3);
        createFollowRelationship(user7, user1);

        log.info("Created follow relationships");
    }

    private void createFollowRelationship(Member follower, Member following) {
        Follow follow = Follow.builder()
            .follower(follower)
            .following(following)
            .build();
        follower.getFollowings().add(follow);
        following.getFollowers().add(follow);
    }

    @Transactional
    public void settingAladinBooks() {
        // 이미 책이 존재하는지 확인
        if (aladinBookRepository.findByIsbn13("9791165341909").isPresent()) {
            log.info("Test book data already exists. Skipping book initialization.");
            return;
        }

        List<AladinBook> books = new ArrayList<>();

        // 1. 소설 - 달러구트 꿈 백화점
        books.add(AladinBook.builder()
            .isbn13("9791165341909")
            .title("달러구트 꿈 백화점")
            .author("이미예")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=247270655")
            .cover("https://image.aladin.co.kr/product/24727/6/cover/k582730586_1.jpg")
            .fullDescription(
                "이미예 작가의 장편소설. 잠들어야만 입장할 수 있는 신비로운 상점, '달러구트 꿈 백화점'을 통해 꿈을 사고파는 가게의 비밀스러운 이야기를 그려낸 판타지 소설. 꿈을 통해 잃어버린 희망을 되찾고 새로운 삶의 의미를 발견하는 감동적인 이야기.")
            .publisher("팩토리나인")
            .categoryName("국내도서>소설")
            .customerReviewRank(4.8)
            .itemPage(300)
            .build());

        // 2. 자기계발 - 원씽
        books.add(AladinBook.builder()
            .isbn13("9788901219943")
            .title("원씽(The One Thing)")
            .author("게리 켈러, 제이 파파산")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=71895491")
            .cover("https://image.aladin.co.kr/product/7189/54/cover/8901219948_1.jpg")
            .fullDescription("단순함의 힘을 강조하는 자기계발서. 성공을 위해 가장 중요한 한 가지에 집중하라는 메시지를 전달한다.")
            .publisher("비즈니스북스")
            .categoryName("국내도서>자기계발")
            .customerReviewRank(4.7)
            .itemPage(280)
            .build());

        // 3. 과학 - 코스모스
        books.add(AladinBook.builder()
            .isbn13("9788983711892")
            .title("코스모스")
            .author("칼 세이건")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=1752")
            .cover("https://image.aladin.co.kr/product/175/2/cover/8983711892_1.jpg")
            .fullDescription(
                "우주의 탄생부터 현재까지, 그리고 미래까지 아우르는 장대한 우주 여행. 과학의 역사와 함께 인류의 미래까지 전망하는 과학 교양서의 고전.")
            .publisher("사이언스북스")
            .categoryName("국내도서>과학")
            .customerReviewRank(4.9)
            .itemPage(450)
            .build());

        // 4. 경제경영 - 부의 추월차선
        books.add(AladinBook.builder()
            .isbn13("9788965962274")
            .title("부의 추월차선")
            .author("엠제이 드마코")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=6574537")
            .cover("https://image.aladin.co.kr/product/657/45/cover/8965962277_1.jpg")
            .fullDescription("진정한 부자가 되기 위한 현실적인 방법을 제시하는 책. 저자는 부자가 되는 길은 결코 일반적인 방법이 아님을 강조한다.")
            .publisher("청림출판")
            .categoryName("국내도서>경제경영")
            .customerReviewRank(4.6)
            .itemPage(388)
            .build());

        // 5. 컴퓨터IT - 클린 코드
        books.add(AladinBook.builder()
            .isbn13("9788966260959")
            .title("Clean Code 클린 코드")
            .author("로버트 C. 마틴")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=34083680")
            .cover("https://image.aladin.co.kr/product/3408/36/cover/8966260959_1.jpg")
            .fullDescription("로버트 마틴의 Clean Code는 오늘날 프로그래머들에게 가장 중요한 참고 도서가 되었다.")
            .publisher("인사이트")
            .categoryName("국내도서>컴퓨터/IT")
            .customerReviewRank(4.8)
            .itemPage(584)
            .build());

        // 6. 인문학 - 사피엔스
        books.add(AladinBook.builder()
            .isbn13("9788934972464")
            .title("사피엔스")
            .author("유발 하라리")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=44168331")
            .cover("https://image.aladin.co.kr/product/4416/83/cover/8934972467_2.jpg")
            .fullDescription("인류의 역사를 생물학, 경제학, 종교, 심리학 등 다양한 관점에서 통찰력 있게 분석한 역작")
            .publisher("김영사")
            .categoryName("국내도서>인문학")
            .customerReviewRank(4.7)
            .itemPage(643)
            .build());

        // 7. 시/에세이 - 흔한남매
        books.add(AladinBook.builder()
            .isbn13("9791164137282")
            .title("흔한남매 11")
            .author("백난도")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=308986523")
            .cover("https://image.aladin.co.kr/product/30898/65/cover/k582835377_1.jpg")
            .fullDescription("유튜브 구독자 250만의 흔한남매! 흔한남매의 일상 속 에피소드를 담은 에세이")
            .publisher("미래엔")
            .categoryName("국내도서>시/에세이")
            .customerReviewRank(4.9)
            .itemPage(168)
            .build());

        // 8. 요리 - 백종원
        books.add(AladinBook.builder()
            .isbn13("9791187142560")
            .title("백종원이 추천하는 집밥 메뉴 55")
            .author("백종원")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=56162231")
            .cover("https://image.aladin.co.kr/product/5616/22/cover/k212434773_1.jpg")
            .fullDescription("대한민국 대표 요리연구가 백종원이 추천하는 집밥 메뉴 55가지 레시피")
            .publisher("서울문화사")
            .categoryName("국내도서>요리")
            .customerReviewRank(4.5)
            .itemPage(264)
            .build());

        // 9. 건강/스포츠 - 다이어트
        books.add(AladinBook.builder()
            .isbn13("9791190382083")
            .title("옆구리 삼겹살을 부탁해")
            .author("린다")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=276247392")
            .cover("https://image.aladin.co.kr/product/27624/73/cover/k032731435_1.jpg")
            .fullDescription("유튜버 린다의 실속있는 홈트레이닝 가이드")
            .publisher("북로그컴퍼니")
            .categoryName("국내도서>건강/스포츠")
            .customerReviewRank(4.6)
            .itemPage(280)
            .build());

        // 10. 만화 - 슬램덩크
        books.add(AladinBook.builder()
            .isbn13("9788925864815")
            .title("슬램덩크 신장재편판 1")
            .author("이노우에 다케히코")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=284529433")
            .cover("https://image.aladin.co.kr/product/28452/94/cover/k582730341_1.jpg")
            .fullDescription("농구 만화의 전설, 슬램덩크가 더욱 선명하고 실감나게 돌아왔다!")
            .publisher("대원씨아이")
            .categoryName("국내도서>만화")
            .customerReviewRank(4.9)
            .itemPage(248)
            .build());

        // 11. 한강 - 소년이 온다
        books.add(AladinBook.builder()
            .isbn13("9788936434120")
            .title("소년이 온다 - 2024 노벨문학상 수상작가")
            .author("한강 (지은이)")
            .link(
                "http://www.aladin.co.kr/shop/wproduct.aspx?ItemId=40869703&amp;partner=openAPI&amp;start=api")
            .cover("https://image.aladin.co.kr/product/4086/97/coversum/8936434128_2.jpg")
            .fullDescription(
                "섬세한 감수성과 치밀한 문장으로 인간 존재의 본질을 탐구해온 작가 한강의 여섯번째 장편소설. '상처의 구조에 대한 투시와 천착의 서사'를 통해 한강만이 풀어낼 수 있는 방식으로 1980년 5월을 새롭게 조명한다.")
            .publisher("창비")
            .categoryName("국내도서>소설")
            .customerReviewRank(10.0)
            .build());

        // 모든 책 저장
        aladinBookRepository.saveAll(books);
        log.info("Saved {} test books", books.size());
    }

    @Transactional
    public void settingMyBooks() {
        // 테스트용 멤버와 책 정보 가져오기
        Member reader1 = memberRepository.findByUsername("reader1@gmail.com").orElseThrow();
        Member bookworm = memberRepository.findByUsername("bookworm@naver.com").orElseThrow();
        Member scienceUser = memberRepository.findByUsername("science@gmail.com").orElseThrow();

        // 이미 MyBook 데이터가 있는지 확인
        if (myBookRepository.findByMemberAndAladinBook(reader1,
            aladinBookRepository.findByIsbn13("9791165341909").orElseThrow()).isPresent()) {
            log.info("Test MyBook data already exists. Skipping MyBook initialization.");
            return;
        }

        List<MyBook> myBooks = new ArrayList<>();

        // 1. reader1의 완독한 책 (달러구트 꿈 백화점)
        myBooks.add(MyBook.builder()
            .member(reader1)
            .aladinBook(aladinBookRepository.findByIsbn13("9791165341909").orElseThrow())
            .isbn13("9791165341909")
            .status(BookStatus.COMPLETED)
            .myRating(4.5)
            .oneLineReview("정말 재미있게 읽었어요! 꿈에 대한 새로운 시각을 갖게 되었습니다.")
            .currentPage(300)
            .updateCount(5)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 1, 15))
            .build());

        // 2. reader1의 읽는 중인 책 (사피엔스)
        myBooks.add(MyBook.builder()
            .member(reader1)
            .aladinBook(aladinBookRepository.findByIsbn13("9788934972464").orElseThrow())
            .isbn13("9788934972464")
            .status(BookStatus.READING)
            .currentPage(300)
            .updateCount(3)
            .startDate(LocalDate.of(2024, 2, 1))
            .build());

        // 3. bookworm의 완독한 책 (사피엔스)
        myBooks.add(MyBook.builder()
            .member(bookworm)
            .aladinBook(aladinBookRepository.findByIsbn13("9788934972464").orElseThrow())
            .isbn13("9788934972464")
            .status(BookStatus.COMPLETED)
            .myRating(5.0)
            .oneLineReview("인류 역사에 대한 통찰력 있는 분석, 강력 추천합니다!")
            .currentPage(643)
            .updateCount(8)
            .startDate(LocalDate.of(2024, 1, 10))
            .endDate(LocalDate.of(2024, 2, 10))
            .build());

        // 4. scienceUser의 읽는 중인 책 (코스모스)
        myBooks.add(MyBook.builder()
            .member(scienceUser)
            .aladinBook(aladinBookRepository.findByIsbn13("9788983711892").orElseThrow())
            .isbn13("9788983711892")
            .status(BookStatus.READING)
            .currentPage(200)
            .updateCount(4)
            .startDate(LocalDate.of(2024, 2, 15))
            .oneLineReview("우주의 신비로움을 느낄 수 있는 책")
            .build());

        // 5. reader1의 읽고 싶은 책 (원씽)
        myBooks.add(MyBook.builder()
            .member(reader1)
            .aladinBook(aladinBookRepository.findByIsbn13("9788901219943").orElseThrow())
            .isbn13("9788901219943")
            .status(BookStatus.WISH)
            .build());

        // 6. bookworm의 중단한 책 (클린 코드)
        myBooks.add(MyBook.builder()
            .member(bookworm)
            .aladinBook(aladinBookRepository.findByIsbn13("9788966260959").orElseThrow())
            .isbn13("9788966260959")
            .status(BookStatus.STOPPED)
            .currentPage(200)
            .updateCount(2)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 1, 20))
            .oneLineReview("어려워서 잠시 중단...")
            .build());

        // 7. scienceUser의 완독한 책 (클린 코드)
        myBooks.add(MyBook.builder()
            .member(scienceUser)
            .aladinBook(aladinBookRepository.findByIsbn13("9788966260959").orElseThrow())
            .isbn13("9788966260959")
            .status(BookStatus.COMPLETED)
            .myRating(4.8)
            .oneLineReview("개발자라면 꼭 읽어야 할 책!")
            .currentPage(584)
            .updateCount(10)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 2, 1))
            .build());

        // 8. bookworm의 읽고 싶은 책 (부의 추월차선)
        myBooks.add(MyBook.builder()
            .member(bookworm)
            .aladinBook(aladinBookRepository.findByIsbn13("9788965962274").orElseThrow())
            .isbn13("9788965962274")
            .status(BookStatus.WISH)
            .build());

        // 9. reader1의 중단한 책 (슬램덩크)
        myBooks.add(MyBook.builder()
            .member(reader1)
            .aladinBook(aladinBookRepository.findByIsbn13("9788925864815").orElseThrow())
            .isbn13("9788925864815")
            .status(BookStatus.STOPPED)
            .currentPage(100)
            .updateCount(1)
            .startDate(LocalDate.of(2024, 2, 1))
            .endDate(LocalDate.of(2024, 2, 10))
            .build());

        // 10. scienceUser의 읽고 싶은 책 (사피엔스)
        myBooks.add(MyBook.builder()
            .member(scienceUser)
            .aladinBook(aladinBookRepository.findByIsbn13("9788934972464").orElseThrow())
            .isbn13("9788934972464")
            .status(BookStatus.WISH)
            .build());

        // 11. scienceUser의 읽고 싶은 책 (소년이 온다)
        myBooks.add(MyBook.builder()
            .member(scienceUser)
            .aladinBook(aladinBookRepository.findByIsbn13("9788936434120").orElseThrow())
            .isbn13("9788936434120")
            .status(BookStatus.WISH)
            .build());

        // 모든 MyBook 저장
        myBookRepository.saveAll(myBooks);
        log.info("Saved {} test myBooks", myBooks.size());
    }
}
