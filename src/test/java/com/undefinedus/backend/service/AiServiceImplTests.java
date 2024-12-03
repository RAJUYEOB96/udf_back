package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionGPTResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


@Disabled("API 키가 필요한 테스트 - 로컬에서 API-KEY로 확인 후 올림, 테스트 application.yml은 키가 없어서 에러남")
@SpringBootTest
@Log4j2
@Transactional
class AiServiceImplTests {
    
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private WebClient webClient;

    @Autowired
    private AiService aiService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Autowired
    private MyBookRepository myBookRepository;

    private Discussion testDiscussion;
    private Member testMember;
    private MyBook testMyBook;
    private AladinBook testAladinBook;
    
    @Value("${spring.ai.openai.api-key}")
    String apiKey;


    @BeforeEach
    void setUp() {
        // AladinBook 생성
        testAladinBook = AladinBook.builder()
            .isbn13("9999")
            .title("test")
            .author("유발 test")
            .link("https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=44168331")
            .cover("https://image.aladin.co.kr/product/4416/83/cover/8934972467_2.jpg")
            .fullDescription("인류의 역사를 생물학, 경제학, 종교, 심리학 등 다양한 관점에서 통찰력 있게 분석한 역작")
            .fullDescription2("test")
            .publisher("김영사")
            .categoryName("국내도서>인문학")
            .customerReviewRank(4.7)
            .itemPage(643)
            .build();

        aladinBookRepository.save(testAladinBook);


        // Member 생성
        testMember = Member.builder()
            .username("test@test.com")
            .password("password123")
            .nickname("tester")
            .memberRoleList(List.of(MemberType.USER))
            .isPublic(true)
            .build();
        memberRepository.save(testMember);

        // MyBook 생성
        testMyBook = MyBook.builder()
            .member(testMember)
            .aladinBook(testAladinBook)
            .isbn13(testAladinBook.getIsbn13())
            .status(BookStatus.COMPLETED)
            .myRating(4.5)
            .oneLineReview("아 집에가고 싶다")
            .currentPage(10)
            .startDate(LocalDate.now().minusDays(1))
            .endDate(LocalDate.now())
            .build();

        myBookRepository.save(testMyBook);

        // Discussion 생성
        testDiscussion = Discussion.builder()
            .title("'사피엔스'가 제시하는 인류의 미래는?")
            .content("유발 하라리가 그리는 인류의 미래상에 대해 어떻게 생각하시나요?")
            .member(testMember)
            .myBook(testMyBook)
            .build();

        discussionRepository.save(testDiscussion);
    }

    @Test
    @DisplayName("실제 ChatGPT 연결 테스트")
    public void testRealChatGPTConnection() {
        // 기존 Mock 제거
        ChatClient realChatClient = chatClient;  // 실제 주입된 ChatClient 사용

        try {
            // Given
            String testPrompt = "Say hello in one word";

            // When
            String response = realChatClient.prompt()
                .user(userSpec -> userSpec.text(testPrompt))
                .call()
                .content();

            // Then
            assertNotNull(response);
            assertFalse(response.isEmpty());
            System.out.println("Real ChatGPT Response: " + response);

        } catch (Exception e) {
            fail("ChatGPT connection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("ChatGPT API Key 설정 테스트")
    public void testChatGPTConfiguration() {
        // Given

        // Then
        assertNotNull(apiKey, "API Key should not be null");
        assertFalse(apiKey.isEmpty(), "API Key should not be empty");
        assertTrue(apiKey.startsWith("sk-"), "API Key should start with 'sk-'");
    }

    @Test
    @DisplayName("테스트")
    @Transactional
    public void preference() {

        Long memberId = 2L;

        Optional<Member> opt = memberRepository.findById(memberId);
        Member member = opt.get();
        Set<PreferencesType> ps = member.getPreferences();
        Iterator<PreferencesType> iter = ps.iterator();

        System.out.println(ps);
        while (iter.hasNext()) {
            PreferencesType pt = iter.next();
            System.out.println(pt.getCategoryId());
        }
    }

    @Test
    @DisplayName("gpt 추천 도서 테스트")
    public void getGPTRecommendedBookList() {

        Long memberId = 2L;

        List<AladinApiResponseDTO> booksByIsbn = aiService.getPerplexityRecommendBookList(
            memberId);

        System.out.println(booksByIsbn);
        System.out.println(booksByIsbn.size());
    }

    @Test
    @DisplayName("gpt 토론 분석 및 출력")
    public void getDiscussionGPT() throws IOException {

        // When
        aiService.discussionInfoToGPT(testDiscussion.getId());

        // Then
        DiscussionGPTResponseDTO result = aiService.getDiscussionGPTResult(testDiscussion.getId());

        assertNotNull(result);
        System.out.println("result = " + result);
    }
}