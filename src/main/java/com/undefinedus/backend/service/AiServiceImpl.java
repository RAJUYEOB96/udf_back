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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
        List<String> isbnList = new ArrayList<>();

        while (allBooks.size() < 5) {
            // Perplexity API에서 ISBN 목록 가져오기
            List<String> isbn13List = memberBookIsbn13ListToPerplexity(memberId);

            isbnList.addAll(isbn13List);

            for (String isbn : isbnList) {
                List<AladinApiResponseDTO> books = aladinBookService.detailAladinAPI(isbn);
                if (books != null && !books.isEmpty()) {
                    allBooks.add(books.get(0));
                    if (allBooks.size() >= 5) {
                        break;
                    }
                }
            }

            // 이미 처리한 ISBN 제거
            isbnList.clear();
        }

        return allBooks.subList(0, Math.min(allBooks.size(), 5));
    }

    // Perplexity는 검색이 가능해서 isbn13을 추천 받을 수 있음
    private List<String> memberBookIsbn13ListToPerplexity(Long memberId) {
        List<String> best5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);
        String isbn13List = String.join(", ", best5Isbn13ByMemberId);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-sonar-small-128k-online");
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content", "Search for books similar to the ones I provide by their ISBN13 numbers, " +
                "and recommend exactly five different books. " +
                "The recommended books must be different from the provided books and should not have duplicate ISBN13 numbers among them. " +
                "Please ensure the books are available on Aladin, focusing specifically on domestic (Korean) publications. " +
                "If the provided books belong to fewer than five categories, you may select books from available categories " +
                "and allow 2 to 4 categories to be duplicated to ensure a total of five recommendations " +
                "while including all categories from the provided books. " +
                "If the books belong to five or more categories, select one book per category for a total of five recommendations. " +
                "Output only the ISBN13 numbers of the five recommended books, each on a new line, without any prefixes, explanations, or labels. "
                + "Please recommend new ISBN numbers different from the provided ISBN13s." +
                "The isbn13 list is: " + isbn13List)
        ));

        System.out.println("isbn13List = " + isbn13List);

        String response = webClient.post()
            .uri(API_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        System.out.println("response = " + response);

        return parseIsbn13FromAnswer(response);
    }

    private List<String> parseIsbn13FromAnswer(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
            return Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(isbn -> !isbn.isEmpty())
                .collect(Collectors.toList());
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
            discussion.getMyBook().getAladinBook());

        String information = formatAladinBookToJson(aladinBookDTO); // 변환된 DTO를 JSON으로 변환

        String title = discussion.getTitle();

        String content = discussion.getContent();

        String agreeListJson = formatCommentsToJson(discussion, VoteType.AGREE);

        String disagreeListJson = formatCommentsToJson(discussion, VoteType.DISAGREE);

        String promptText = String.format("""
            Analyze the provided information about a book, a discussion's title, and the content, along with categorized comments. Your task is to provide the following analysis:
                        
            - A summary conclusion of the discussion.
            - A final decision on the overall sentiment:
              - **true** if the majority of comments are in favor, and the reasoning supports this conclusion.
              - **false** if the majority of comments are against, and the reasoning supports this conclusion.
              - **null** if the comments are evenly split between in favor and against, or if the reasoning does not clearly support one side.
            - The percentage of comments that are in favor (agreePercent) as an integer. If there are no comments, set this to 0.
            - The percentage of comments that are against (disagreePercent) calculated as 100 - agreePercent. If there are no comments, set this to 0.
            - A reasoning explaining the decision based on the comments, with the majority opinion and the sentiment behind the arguments considered.
                        
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
