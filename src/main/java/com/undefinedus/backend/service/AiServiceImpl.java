package com.undefinedus.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinBookForGPTResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionGPTResponseDTO;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final MyBookRepository myBookRepository;
    private final ChatClient chatClient;
    private final AladinBookService aladinBookService;
    private final DiscussionRepository discussionRepository;
    private final WebClient webClient;

    private static final String API_URL = "https://api.perplexity.ai/chat/completions";

    @Value("${spring.ai.perplexity.api-key}")
    private String apiKey;

    @Override
    public List<AladinApiResponseDTO> getPerplexityRecommendBookList(Long memberId) {
        List<AladinApiResponseDTO> allBooks = new ArrayList<>();
        Set<String> processedIsbns = new HashSet<>(); // 이미 처리한 ISBN 저장

        while (allBooks.size() < 5) {
            // Perplexity API에서 ISBN 목록 가져오기
            List<String> isbn13List = memberBookIsbn13ListToPerplexity(memberId);

            for (String isbn : isbn13List) {

                System.out.println("isbn = " + isbn);
                // 중복 ISBN 확인
                if (!processedIsbns.contains(isbn)) {
                    List<AladinApiResponseDTO> books = aladinBookService.detailAladinAPI(isbn);

                    System.out.println("books = " + books);
                    if (books != null && !books.isEmpty()) {
                        AladinApiResponseDTO book = books.get(0);

                        // 중복 책 정보 확인
                        if (allBooks.stream()
                            .noneMatch(b -> b.getIsbn13().equals(book.getIsbn13()))) {
                            allBooks.add(book);
                            processedIsbns.add(isbn); // 처리한 ISBN 기록

                            if (allBooks.size() >= 5) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return allBooks.subList(0, Math.min(allBooks.size(), 5));
    }

    // Perplexity는 검색이 가능해서 isbn13을 추천 받을 수 있음
    private List<String> memberBookIsbn13ListToPerplexity(Long memberId) {
        List<String> best5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);
        String isbn13List = String.join(", ", best5Isbn13ByMemberId);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-sonar-huge-128k-online");
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content",
                "Search for books similar to the ones I provide by their ISBN13 numbers, " +
                    "and recommend exactly 20 different books. " +
                    "The recommended books must be different from the provided books and should not have duplicate ISBN13 numbers among them. "
                    +
                    "Please ensure the books are available on Aladin, focusing specifically on domestic (Korean) publications. "
                    +
                    "If the provided books belong to fewer than fifteen categories, you may select books from available categories "
                    +
                    "and allow some categories to be duplicated to ensure a total of fifteen recommendations "
                    +
                    "while including all categories from the provided books. " +
                    "If the books belong to fifteen or more categories, select one book per category for a total of fifteen recommendations. "
                    +
                    "Output only the ISBN13 numbers of the fifteen recommended books, each on a new line, without any prefixes, explanations, or labels. "
                    +
                    "Please recommend new ISBN numbers different from the provided ISBN13s. " +
                    "The isbn13 list is: " + isbn13List)
        ));

        String response = webClient.post()
            .uri(API_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        return parseIsbn13FromAnswer(response);
    }

    private List<String> parseIsbn13FromAnswer(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.path("choices").get(0).path("message").path("content")
                .asText();

            // 쉼표와 공백을 기준으로 ISBN13 분리
            List<String> collect = Arrays.stream(content.split("[,\\s]+")) // 쉼표나 공백으로 분리
                .map(String::trim)
                .filter(isbn -> isbn.matches("\\d+")) // 숫자로만 구성된 값 필터링
                .collect(Collectors.toList());

            return collect;
        } catch (Exception e) {
            throw new RuntimeException("ISBN13 파싱 실패: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    // gpt는 사람의 말을 이해하고 답변하는데 맞추어져 있음
    public void discussionInfoToGPT(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
            .orElseThrow(() -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다."));

        // AladinBook을 DTO로 변환
        AladinBookForGPTResponseDTO aladinBookDTO = convertToAladinBookDTO(
            discussion.getAladinBook());

        String information = formatAladinBookToJson(aladinBookDTO); // 변환된 DTO를 JSON으로 변환

        String title = discussion.getTitle();
        String content = discussion.getContent();

        String agreeListJson = formatCommentsToJson(discussion, VoteType.AGREE);
        String disagreeListJson = formatCommentsToJson(discussion, VoteType.DISAGREE);

        String promptText = String.format("""
            Analyze the provided book information, discussion title, content, and categorized comments. Based on your analysis, provide the following details:

            A summary conclusion of the discussion.
            A final decision on the overall sentiment:
            If the conclusion is in favor, set the result to true.
            If the conclusion is against, set the result to false.
            If the result is null, it means the conclusion is unclear or the favor and against arguments are equally valid. 
            However, if the conclusion leans more towards true, the result should be true, and if the conclusion leans more towards false, the result should be false.
                        
            Based on the conclusion:
            If the conclusion leans more towards being in favor, provide the validity of the favor conclusion as a percentage (agreePercent) and calculate the validity of the against conclusion as 100 - agreePercent (disagreePercent).
            Similarly, if the conclusion leans more towards being against, provide the validity of the against conclusion as a percentage (disagreePercent) and calculate the validity of the favor conclusion as 100 - disagreePercent (agreePercent).
            For example, if the favor conclusion is deemed 80Percent valid, and the against conclusion 20Percent valid, write agreePercent: 80 and disagreePercent: 20.
            If there are no comments, set both agreePercent and disagreePercent to 0.
            Provide reasoning for the conclusion and percentages. Base the reasoning on the comments and arguments provided.
            When the conclusion is null, write all the reasoning and end with "As a result, the overall conclusion is neutral.

            Please provide the output in Korean.

            The response should be in JSON format with this exact structure:

            {
              "conclusion": "your conclusion here",
              "result": true/false/null,
              "agreePercent": number,
              "disagreePercent": number,
              "reasoning": "your reasoning here"
            }

            Here is the information you need to analyze:

            Book Information: %s
            Discussion Title: %s
            Discussion Content: %s
            Agree Comments: %s
            Disagree Comments: %s
            """, information, title, content, agreeListJson, disagreeListJson);

        String answerText = chatClient.prompt()
            .user(userSpec -> userSpec.text(promptText))
            .call()
            .content();

        saveDiscussionChatGptResult(discussion, answerText);
    }

    private String formatAladinBookToJson(AladinBookForGPTResponseDTO aladinBookForGPTResponseDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(aladinBookForGPTResponseDTO);
        } catch (Exception e) {
            throw new RuntimeException("AladinBook JSON 변환 실패 : " + aladinBookForGPTResponseDTO, e);
        }
    }

    private String formatCommentsToJson(Discussion discussion, VoteType voteType) {
        List<Map<String, Object>> comments = discussion.getComments().stream()
            .filter(comment -> comment.getVoteType() == voteType)
            .map(comment -> {
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("commentId", comment.getId());
                jsonMap.put("content", comment.getContent());
                jsonMap.put("voteType", comment.getVoteType().toString());
                return jsonMap;
            })
            .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(comments);
        } catch (Exception e) {
            throw new RuntimeException("댓글 JSON 변환 실패" + discussion.getId(), e);
        }
    }

    private void saveDiscussionChatGptResult(Discussion discussion, String answerText) {
        try {

            // Markdown 코드 블록 제거
            String cleanJson = answerText
                .replaceAll("```json\\s*", "")  // 시작 부분의 ```json 제거
                .replaceAll("```\\s*$", "")     // 끝 부분의 ``` 제거
                .trim();                        // 앞뒤 공백 제거

            ObjectMapper objectMapper = new ObjectMapper();
            DiscussionGPTResponseDTO analysisResult = objectMapper.readValue(cleanJson,
                DiscussionGPTResponseDTO.class);

            discussion.changeConclusion(analysisResult.getConclusion());
            discussion.changeResult(analysisResult.getResult());
            discussion.changeAgreePercent(analysisResult.getAgreePercent());
            discussion.changeDisagreePercent(analysisResult.getDisagreePercent());
            discussion.changeReasoning(analysisResult.getReasoning());

            discussionRepository.save(discussion);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AladinBookForGPTResponseDTO convertToAladinBookDTO(AladinBook aladinBook) {
        return new AladinBookForGPTResponseDTO(
            aladinBook.getIsbn13(),
            aladinBook.getTitle(),
            aladinBook.getAuthor(),
            aladinBook.getLink(),
            aladinBook.getCover(),
            aladinBook.getFullDescription(),
            aladinBook.getFullDescription2(),
            aladinBook.getPublisher(),
            aladinBook.getCategoryName(),
            aladinBook.getCustomerReviewRank(),
            aladinBook.getItemPage()
        );
    }

    @Override
    public DiscussionGPTResponseDTO getDiscussionGPTResult(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
            .orElseThrow(
                () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));

        return DiscussionGPTResponseDTO.builder()
            .conclusion(discussion.getConclusion())
            .result(discussion.getResult())
            .agreePercent(discussion.getAgreePercent())
            .disagreePercent(discussion.getDisagreePercent())
            .reasoning(discussion.getReasoning())
            .build();
    }
}
