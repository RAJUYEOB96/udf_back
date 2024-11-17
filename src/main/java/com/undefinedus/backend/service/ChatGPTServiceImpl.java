package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatGPTService {

    private final MyBookRepository myBookRepository;

    private final ChatClient chatClient;

    private final AladinBookService aladinBookService;

    private final MemberRepository memberRepository;

    @Value("classpath:/promptTemplates/questionPromptTemplate.st")
    Resource questionPromptTemplate;

    @Override
    public List<AladinApiResponseDTO> getGPTRecommendedBookLIst(Long memberId) {
        // GPT 추천 ISBN 목록 가져오기
        List<String> isbnList = getGPTRecommendedBookIsbnLIst(memberId);
        System.out.printf("개수: %s", isbnList.size());
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

    private List<String> getGPTRecommendedBookIsbnLIst(Long memberId) {

        List<String> top5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);

        for (String isbn13 : top5Isbn13ByMemberId) {
            System.out.println("isbn13 = " + isbn13);
        }

        String isbn13List = String.join(", ", top5Isbn13ByMemberId);

        String answerText = chatClient.prompt()
            .user(
                userSpec -> userSpec
                    .text(questionPromptTemplate)
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
}
