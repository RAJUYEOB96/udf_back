package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import groovy.util.logging.Slf4j;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
@RequiredArgsConstructor
public class KakaoMessageService {

    private final MemberRepository memberRepository;
    private final MyBookmarkRepository myBookmarkRepository;
    private final String KAKAO_API_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJyb2xlcyI6WyJVU0VSIl0sIm5pY2tuYW1lIjoi7Iug64-ZIiwiaWQiOjE3LCJzb2NpYWxQcm92aWRlciI6IktBS0FPIiwidXNlcm5hbWUiOiJrYWthb18zODEzNzkzODQ4IiwiaWF0IjoxNzMyOTU1NDc5LCJleHAiOjE3MzI5NTcyNzl9.LsmyId152HWSfkqD8wCCBkM61P6VV0_yxLEZPtYxg-NULWTx9UtRQVqLhYg3SlL9";
    private final String KAKAO_API_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";


    public void sendKakaoTalk() {

        List<Long> kakaoMemberIdList = memberRepository.findMessageToKakaoMemberIdList();

        for (Long memberId : kakaoMemberIdList) {

            Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다 : " + memberId));

            String refreshToken = member.getKakaoRefreshToken();

//            String kakaoAccessToken = member.getKakaoAccessToken();

            List<MyBookmark> myBookmarkList = myBookmarkRepository.findByMemberId(memberId);

            int size = myBookmarkList.size();

            int index = (int) (Math.random() * size);

            MyBookmark randomMyBookmark = myBookmarkList.get(index);

            String phrase = randomMyBookmark.getPhrase();

            sendToMe(phrase, refreshToken);
        }
    }

    private void sendToMe(String message, String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + kakaoAccessToken);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("template_object", String.format(
            "{\"object_type\": \"text\", \"text\": \"%s\", \"link\": {"
                + "\"web_url\": \"http://localhost:5173\","
                + "\"mobile_web_url\": \"http://localhost:5173\""
                + "}}", message));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            KAKAO_API_URL,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        log.info("Message sent: {}", response.getStatusCode());
    }
}
