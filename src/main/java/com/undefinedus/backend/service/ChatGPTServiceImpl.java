package com.undefinedus.backend.service;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatGPTService {

    private final MyBookRepository myBookRepository;
    private final ChatClient chatClient;
    private final AladinBookService aladinBookService;
    private final DiscussionRepository discussionRepository;

    @Value("classpath:/promptTemplates/questionPromptTemplate.st")
    Resource questionPromptTemplate;

    @Value("classpath:/promptTemplates/testPromptTemplate.st")
    Resource testPromptTemplate;

    @Override
    public List<AladinApiResponseDTO> getGPTRecommendedBookList(Long memberId) {
        // GPT 추천 ISBN 목록 가져오기
        List<String> isbnList = getGPTRecommendedBookIsbn13List(memberId);

        // ISBN 리스트에서 각 ISBN에 대해 Aladin API를 호출하여 결과를 합침
        List<AladinApiResponseDTO> allBooks = new ArrayList<>();
        for (String isbn : isbnList) {
            // 각 ISBN을 개별적으로 검색
            List<AladinApiResponseDTO> books = aladinBookService.detailAladinAPI(isbn);
            if (books != null) {
                allBooks.addAll(books);
                if (allBooks.size() >= 5) {
                    break;
                }
            } // 각 검색 결과를 모두 합침
        }

        return allBooks; // 최종적으로 모든 책을 반환
    }

    private List<String> getGPTRecommendedBookIsbn13List(Long memberId) {

        List<String> top5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);

        String isbn13List = String.join(", ", top5Isbn13ByMemberId);

        String answerText = chatClient.prompt()
            .user(
                userSpec -> userSpec
                    .text(testPromptTemplate)
                    .param("isbn13", isbn13List))
            .call()
            .content();

        System.out.println("answerText = " + answerText);

        return parseIsbn13FromAnswer(answerText);
    }

    private List<String> parseIsbn13FromAnswer(String answerText) {
        System.out.println("answerText = " + answerText);

        // "isbn :"을 제거하고, 쉼표로 구분된 ISBN 번호만 추출
        return List.of(answerText.replace("isbn :", "").split("\\s+"))
            .stream()
            .map(String::trim)  // 공백 제거
            .filter(isbn -> !isbn.isEmpty())  // 빈 문자열은 제외
            .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void discussionInfoToGPT(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
            .orElseThrow(() -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다."));

        // AladinBook을 DTO로 변환
        AladinBookForGPTResponseDTO aladinBookDTO = convertToAladinBookDTO(
            discussion.getMyBook().getAladinBook());

        String information = formatAladinBookToJson(aladinBookDTO); // 변환된 DTO를 JSON으로 변환

        System.out.println("information = " + information);

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
