package com.undefinedus.backend.config;


import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.CalendarStampRepository;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
public class InitialDataConfig {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AladinBookRepository aladinBookRepository;
    private final MyBookRepository myBookRepository;
    private final CalendarStampRepository calendarStampRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionParticipantRepository discussionParticipantRepository;
    private final DiscussionCommentRepository discussionCommentRepository;

    @PostConstruct
    public void initData() {
        try {
            if (memberRepository.count() > 0) {
                log.info("Data already exists. Skipping initialization.");
                return;
            }
            
            // 각 단계별 초기화 실행
            settingMembers();
            validateMembers();
            
            settingAladinBooks();
            validateBooks();
            
            settingMyBooks();
            validateMyBooks();
            
            settingCalendarStamp();
            validateCalendarStamps();
            
            settingDiscussions();
            validateDiscussions();
            
            settingDiscussionComments();
            validateComments();
            
            log.info("All data initialized successfully");
        } catch (Exception e) {
            log.error("Error during data initialization: ", e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }
    
    private void validateMembers() {
        if (memberRepository.count() == 0) {
            throw new RuntimeException("Member initialization failed");
        }
        log.info("Member initialization validated successfully");
    }
    
    private void validateBooks() {
        if (aladinBookRepository.count() == 0) {
            throw new RuntimeException("Book initialization failed");
        }
        log.info("Book initialization validated successfully");
    }
    
    private void validateMyBooks() {
        if (myBookRepository.count() == 0) {
            throw new RuntimeException("MyBook initialization failed");
        }
        log.info("MyBook initialization validated successfully");
    }
    
    private void validateCalendarStamps() {
        if (calendarStampRepository.count() == 0) {
            throw new RuntimeException("CalendarStamp initialization failed");
        }
        log.info("CalendarStamp initialization validated successfully");
    }
    
    private void validateDiscussions() {
        if (discussionRepository.count() == 0) {
            throw new RuntimeException("Discussion initialization failed");
        }
        log.info("Discussion initialization validated successfully");
    }
    
    private void validateComments() {
        if (discussionCommentRepository.count() == 0) {
            throw new RuntimeException("Discussion comment initialization failed");
        }
        log.info("Discussion comment initialization validated successfully");
    }
    
    @Transactional
    public void settingMembers() {
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
                .preferences(Set.of(PreferencesType.소설_시_희곡, PreferencesType.과학, PreferencesType.컴퓨터_모바일))
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
                .preferences(Set.of(PreferencesType.소설_시_희곡, PreferencesType.예술_대중문화, PreferencesType.에세이))
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
                .preferences(Set.of(PreferencesType.컴퓨터_모바일, PreferencesType.경제경영, PreferencesType.자기계발))
                .isPublic(true)
                .isMessageToKakao(false)
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
        
        // 일반 사용자 3 (novel@daum.net) 추가
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
        
        // science@gmail.com 사용자 추가
        Member scienceUser = Member.builder()
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
        members.add(scienceUser);
        
        memberRepository.saveAll(members);
        memberRepository.flush();
        
        // 팔로우 관계 설정
        createFollowRelationships(members);
        log.info("Saved {} test members with follow relationships", members.size());
    }
    
    @Transactional
    protected void createFollowRelationships(List<Member> members) {
        try {
            Member user1 = memberRepository.findByUsernameWithFollows("reader1@gmail.com")
                    .orElseThrow(() -> new RuntimeException("User1 not found"));
            Member user2 = memberRepository.findByUsernameWithFollows("bookworm@naver.com")
                    .orElseThrow(() -> new RuntimeException("User2 not found"));
            Member user3 = memberRepository.findByUsernameWithFollows("novel@daum.net")
                    .orElseThrow(() -> new RuntimeException("User3 not found"));
            
            // 각각의 followings, followers 리스트를 새로운 HashSet으로 초기화
            user1.getFollowings().clear();
            user1.getFollowers().clear();
            user2.getFollowings().clear();
            user2.getFollowers().clear();
            user3.getFollowings().clear();
            user3.getFollowers().clear();
            
            memberRepository.saveAll(Arrays.asList(user1, user2, user3));
            memberRepository.flush();
            
            // 각 팔로우 관계에 대해 개별적으로 생성 및 저장
            Follow follow1 = createAndSaveFollowRelationship(user1, user2);
            Follow follow2 = createAndSaveFollowRelationship(user1, user3);
            Follow follow3 = createAndSaveFollowRelationship(user2, user1);
            Follow follow4 = createAndSaveFollowRelationship(user3, user1);
            
            log.info("Follow relationships created successfully");
        } catch (Exception e) {
            log.error("Error in createFollowRelationships: ", e);
            throw e;
        }
    }
    
    private Follow createAndSaveFollowRelationship(Member follower, Member following) {
        // 이미 존재하는지 확인
        boolean exists = follower.getFollowings().stream()
                .anyMatch(f -> f.getFollowing().equals(following));
        
        if (!exists) {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            follower.getFollowings().add(follow);
            following.getFollowers().add(follow);
            return follow;
        }
        return null;
    }
    
    @Transactional
    public void settingAladinBooks() {
        if (aladinBookRepository.findByIsbn13("9791165341909").isPresent()) {
            log.info("Test book data already exists. Skipping book initialization.");
            return;
        }
        
        List<AladinBook> books = createAladinBooks();
        aladinBookRepository.saveAll(books);
        aladinBookRepository.flush();
        log.info("Saved {} test books", books.size());
    }
    
    private List<AladinBook> createAladinBooks() {
        List<AladinBook> books = new ArrayList<>();
        
        // 달러구트 꿈 백화점
        books.add(AladinBook.builder()
                .isbn13("9791165341909")
                .title("달러구트 꿈 백화점")
                .author("이미예")
                .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=247270655")
                .cover("https://image.aladin.co.kr/product/24727/6/cover/k582730586_1.jpg")
                .fullDescription("이미예 작가의 장편소설. 잠들어야만 입장할 수 있는 신비로운 상점...")
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .fullDescription2("test")
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
                .link("http://www.aladin.co.kr/shop/wproduct.aspx?ItemId=40869703&amp;partner=openAPI&amp;start=api")
                .cover("https://image.aladin.co.kr/product/4086/97/coversum/8936434128_2.jpg")
                .fullDescription("섬세한 감수성과 치밀한 문장으로 인간 존재의 본질을 탐구해온 작가 한강의 여섯번째 장편소설. '상처의 구조에 대한 투시와 천착의 서사'를 통해 한강만이 풀어낼 수 있는 방식으로 1980년 5월을 새롭게 조명한다.")
                .fullDescription2("test")
                .publisher("창비")
                .categoryName("국내도서>소설")
                .customerReviewRank(10.0)
                .itemPage(100)
                .build());
        
        return books;
    }
    
    @Transactional
    public void settingMyBooks() {
        Member reader1 = memberRepository.findByUsername("reader1@gmail.com").orElseThrow();
        
        if (myBookRepository.findByMemberAndAladinBook(reader1,
                aladinBookRepository.findByIsbn13("9791165341909").orElseThrow()).isPresent()) {
            log.info("Test MyBook data already exists. Skipping MyBook initialization.");
            return;
        }
        
        List<MyBook> myBooks = createMyBooks();
        myBookRepository.saveAll(myBooks);
        myBookRepository.flush();
        log.info("Saved {} test myBooks", myBooks.size());
    }
    
    private List<MyBook> createMyBooks() {
        List<MyBook> myBooks = new ArrayList<>();
        
        Member reader1 = memberRepository.findByUsername("reader1@gmail.com").orElseThrow();
        Member bookworm = memberRepository.findByUsername("bookworm@naver.com").orElseThrow();
        Member scienceUser = memberRepository.findByUsername("science@gmail.com").orElseThrow();
        
        // reader1의 완독한 책
        myBooks.add(MyBook.builder()
                .member(reader1)
                .aladinBook(aladinBookRepository.findByIsbn13("9791165341909").orElseThrow())
                .isbn13("9791165341909")
                .status(BookStatus.COMPLETED)
                .myRating(4.5)
                .oneLineReview("정말 재미있게 읽었어요!")
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
        
        return myBooks;
    }
    
    @Transactional
    public void settingCalendarStamp() {
        if (calendarStampRepository.count() > 0) {
            log.info("Test CalendarStamp data already exists. Skipping initialization.");
            return;
        }
        
        List<CalendarStamp> stamps = new ArrayList<>();
        
        // MyBook과 AladinBook을 함께 조회
        List<MyBook> myBooks = myBookRepository.findAllWithAladinBook();
        
        myBooks.stream()
                .filter(myBook -> myBook.getStatus() == BookStatus.READING ||
                        myBook.getStatus() == BookStatus.COMPLETED)
                .forEach(myBook -> {
                    if (myBook.getStartDate() != null) {
                        stamps.add(createCalendarStamp(myBook, myBook.getStartDate(), 1));
                    }
                    if (myBook.getStatus() == BookStatus.COMPLETED && myBook.getEndDate() != null) {
                        stamps.add(createCalendarStamp(myBook, myBook.getEndDate(), 2));
                    }
                });
        
        calendarStampRepository.saveAll(stamps);
        calendarStampRepository.flush();
        log.info("Saved {} calendar stamps", stamps.size());
    }
    
    private CalendarStamp createCalendarStamp(MyBook myBook, LocalDate recordedAt, Integer readDateCount) {
        return CalendarStamp.builder()
                .member(myBook.getMember())
                .myBookId(myBook.getId())
                .bookTitle(myBook.getAladinBook().getTitle())
                .bookAuthor(myBook.getAladinBook().getAuthor())
                .bookCover(myBook.getAladinBook().getCover())
                .recordedAt(recordedAt)
                .status(myBook.getStatus())
                .itemPage(myBook.getAladinBook().getItemPage())
                .currentPage(myBook.getCurrentPage())
                .startDate(myBook.getStartDate())
                .endDate(myBook.getEndDate())
                .readDateCount(readDateCount)
                .build();
    }
    
    // 김용 추가
    @Transactional
    public void settingDiscussions() {
        if (discussionRepository.count() > 0) {
            log.info("Discussion data already exists. Skipping discussion initialization.");
            return;
        }
        
        List<Discussion> discussions = createDiscussions();
        discussionRepository.saveAll(discussions);
        discussionRepository.flush();
        
        // 참여자 추가
        Discussion completedDiscussion = discussions.get(2); // 세 번째 토론
        Member reader1 = memberRepository.findByUsername("reader1@gmail.com").orElseThrow();
        Member bookworm = memberRepository.findByUsername("bookworm@naver.com").orElseThrow();
        addParticipants(completedDiscussion, reader1, bookworm);
        
        log.info("Saved {} test discussions", discussions.size());
    }
    
    private List<Discussion> createDiscussions() {
        List<Discussion> discussions = new ArrayList<>();
        
        Member reader1 = memberRepository.findByUsername("reader1@gmail.com").orElseThrow();
        Member bookworm = memberRepository.findByUsername("bookworm@naver.com").orElseThrow();
        
        MyBook myBook1 = myBookRepository.findByMemberIdAndIsbn13(reader1.getId(), "9791165341909").orElseThrow();
        MyBook myBook2 = myBookRepository.findByMemberIdAndIsbn13(bookworm.getId(), "9788934972464").orElseThrow();
        
        // Discussion 엔티티들 생성...
        // Discussion 1
        discussions.add(Discussion.builder()
                .myBook(myBook1)
                .member(reader1)
                .title("'달러구트 꿈 백화점'에서 가장 인상 깊었던 꿈은?")
                .content("이 책에서 소개된 여러 꿈들 중 가장 기억에 남는 꿈과 그 이유에 대해 이야기해봐요.")
                .status(DiscussionStatus.COMPLETED)
                .startDate(LocalDateTime.now().plusMinutes(10))
                .closedAt(LocalDateTime.now().plusDays(1))
                .build());
        
        // Discussion 2
        discussions.add(Discussion.builder()
                .myBook(myBook2)
                .member(bookworm)
                .title("'사피엔스'가 제시하는 인류의 미래는?")
                .content("유발 하라리가 그리는 인류의 미래상에 대해 어떻게 생각하시나요?")
                .status(DiscussionStatus.COMPLETED)
                .startDate(LocalDateTime.now().plusHours(1))
                .closedAt(LocalDateTime.now().plusDays(1))
                .views(15L)
                .build());
        
        // Discussion 3
        discussions.add(Discussion.builder()  // discussions.add 추가
                .myBook(myBook1)
                .member(reader1)
                .title("꿈을 사고파는 것이 윤리적일까?")
                .content("달러구트 꿈 백화점의 컨셉처럼 꿈을 거래하는 것에 대한 윤리적 문제에 대해 토론해봐요.")
                .status(DiscussionStatus.COMPLETED)
                .startDate(LocalDateTime.now().plusHours(2))
                .closedAt(LocalDateTime.now().plusDays(1))
                .views(50L)
                .build());
        
        return discussions;
    }

    private void addParticipants(Discussion discussion, Member... members) {
        for (Member member : members) {
            DiscussionParticipant participant = DiscussionParticipant.builder()
                .discussion(discussion)
                .member(member)
                .isAgree(Math.random() < 0.5)  // 랜덤하게 찬성/반대 설정
                .build();
            discussionParticipantRepository.save(participant);
        }
        discussionParticipantRepository.flush();
    }
    
    @Transactional
    public void settingDiscussionComments() {
        if (discussionCommentRepository.count() > 0) {
            log.info("Discussion comment data already exists. Skipping initialization.");
            return;
        }
        
        Discussion discussion = discussionRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No discussions found for comment initialization."));
        
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            throw new RuntimeException("No members found for comment initialization.");
        }
        
        List<DiscussionComment> comments = createDiscussionComments(discussion, members);
        discussionCommentRepository.saveAll(comments);
        discussionCommentRepository.flush();
        
        log.info("Saved {} test discussion comments", comments.size());
    }
    
    private List<DiscussionComment> createDiscussionComments(Discussion discussion, List<Member> members) {
        List<DiscussionComment> comments = new ArrayList<>();
        Long totalOrder = 1L;
        
        // 찬성 댓글 1
        DiscussionComment agreeComment1 = createComment(discussion, members.get(0), VoteType.AGREE,
                "이 책의 주제에 대해 전적으로 동의합니다. 저자의 통찰력이 돋보이는 작품이라고 생각합니다.",
                null, 1L, 0L, false, totalOrder++);
        comments.add(agreeComment1);
        
        // 찬성 댓글 2
        comments.add(createComment(discussion, members.get(1), VoteType.AGREE,
                "저도 같은 생각입니다. 특히 3장에서 다룬 내용이 현대 사회의 문제를 정확히 짚어내고 있어 인상 깊었어요.",
                null, 2L, 0L, false, totalOrder++));
        
        // 반대 댓글 1
        DiscussionComment disagreeComment1 = createComment(discussion, members.get(2), VoteType.DISAGREE,
                "저자는 너무 비관적인 시각에서 문제를 바라보고 있는 것 같습니다. 해결책이 비현실적이에요.",
                null, 3L, 0L, false, totalOrder++);
        comments.add(disagreeComment1);
        
        // 반대 댓글 2
        comments.add(createComment(discussion, members.get(3), VoteType.DISAGREE,
                "동감합니다. 특히 저자가 제시한 해결책은 현실에서 적용하기 힘들 것 같아요.",
                null, 4L, 0L, false, totalOrder++));
        
        return comments;
    }
    
    private DiscussionComment createComment(
            Discussion discussion,
            Member member,
            VoteType voteType,
            String content,
            Long parentId,
            Long groupId,
            Long order,
            boolean isChild,
            Long totalOrder) {
        return DiscussionComment.builder()
                .discussion(discussion)
                .member(member)
                .voteType(voteType)
                .content(content)
                .parentId(parentId)
                .groupId(groupId)
                .groupOrder(order)
                .isChild(isChild)
                .totalOrder(totalOrder)
                .build();
    }
}
